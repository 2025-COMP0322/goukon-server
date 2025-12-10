# Redis 채팅 버퍼 설계/변경 요약

## 배경
- WebSocket으로 수신한 채팅 메시지를 즉시 DB에 쓰면 I/O 부하가 커지고, 일시적인 트래픽 폭주 시 병목이 생김.
- 이전 구조는 실시간 전송만 하고, 영속화는 REST `sendMessage` 호출에 한정되어 있어 WebSocket-only 메시지가 DB에 남지 않는 문제가 있었다.

## 주요 변경 (2025-12-08)
1. **Redis 버퍼 도입**
   - `ChatMessageBuffer` 인터페이스와 `RedisChatMessageBuffer` 구현을 추가해, 방별로 `chat:session:{sessionId}` 리스트에 메시지를 적재.
   - `ChatMessage` DTO에 `senderId`를 포함시켜 flush 시 DB에 저장할 수 있도록 함.
2. **Flush 트리거**
   - 방별 버퍼가 10개(`BUFFER_FLUSH_THRESHOLD`) 이상 쌓이거나, REST 조회(`getChatMessages`, `getChatMessagesWithPaging`, `getLastMessage`)가 들어오면 먼저 flush 수행.
   - `ChatMessageFlushScheduler`를 통해 `chat.flush.interval-ms`(기본 5000ms)마다 모든 방을 스캔하여 남은 메시지를 DB로 내림.
3. **트랜잭션/정합성**
   - flush는 `TransactionTemplate`으로 새 트랜잭션에서 실행하며, `MessageType.CHAT` + 유효한 `senderId`만 DB에 저장.
   - Redis 리스트는 FIFO 순서를 보장하도록 pop 시 역순 정렬 후 저장.

## 운영상의 주의점
- WebSocket 클라이언트는 반드시 `senderId`를 포함해 전송해야 flush 시 DB 저장이 이뤄진다.
- Redis 키에 남은 데이터가 많으면 flush 스케줄 간격(`chat.flush.interval-ms`)을 줄이거나 임계치(10개)를 조정한다.
- 장애 분석 시에는 `LRANGE chat:session:{sessionId} 0 -1`로 버퍼 상태를 확인한다.
- 장기적으로는 Redis 퍼시스턴스(AOF) 또는 Kafka 같은 영속 스트림을 추가해 장애 대비성을 높이는 방안을 고려한다.
