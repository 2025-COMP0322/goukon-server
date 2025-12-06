package com.kr.knucampus.presentation.chatroom;

import com.kr.knucampus.application.chat.ChatService;
import com.kr.knucampus.domain.chatroom.ChatRoom;
import com.kr.knucampus.domain.message.Message;
import com.kr.knucampus.global.annotation.AuthUser;
import com.kr.knucampus.presentation.chatroom.dto.response.ChatRoomResponse;
import com.kr.knucampus.presentation.chatroom.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(@AuthUser Long studentId) {
        List<ChatRoom> chatRooms = chatService.getMyChatRooms(studentId);
        List<ChatRoomResponse> responses = chatRooms.stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 채팅방 조회
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long sessionId) {
        ChatRoom chatRoom = chatService.getChatRoom(sessionId);
        return ResponseEntity.ok(ChatRoomResponse.from(chatRoom));
    }

    /**
     * 채팅방 메시지 목록 조회
     */
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<MessageResponse>> getChatMessages(
            @AuthUser Long studentId,
            @PathVariable Long sessionId) {
        List<Message> messages = chatService.getChatMessages(sessionId, studentId);
        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 채팅방 메시지 목록 조회 (페이징)
     */
    @GetMapping("/{sessionId}/messages/paged")
    public ResponseEntity<Page<MessageResponse>> getChatMessagesWithPaging(
            @AuthUser Long studentId,
            @PathVariable Long sessionId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Message> messages = chatService.getChatMessagesWithPaging(sessionId, studentId, pageable);
        Page<MessageResponse> responses = messages.map(MessageResponse::from);
        return ResponseEntity.ok(responses);
    }

    /**
     * 메시지 전송 (REST API)
     */
    @PostMapping("/{sessionId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthUser Long studentId,
            @PathVariable Long sessionId,
            @RequestBody String content) {
        Message message = chatService.saveMessage(sessionId, studentId, content);
        return ResponseEntity.ok(MessageResponse.from(message));
    }
}
