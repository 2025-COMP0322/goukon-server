#!/bin/bash

echo "========================================="
echo "Flyway 히스토리 테이블 삭제"
echo "========================================="

docker exec -i oracle-database sqlplus -s university/tjdudrbs@XE <<'EOF'
SET ECHO OFF
SET FEEDBACK ON
SET SERVEROUTPUT ON
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE "flyway_schema_history" CASCADE CONSTRAINTS';
   DBMS_OUTPUT.PUT_LINE('✓ Flyway 히스토리 테이블이 삭제되었습니다.');
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE = -942 THEN
         DBMS_OUTPUT.PUT_LINE('✓ Flyway 히스토리 테이블이 존재하지 않습니다.');
      ELSE
         RAISE;
      END IF;
END;
/
EXIT;
EOF

echo ""
echo "========================================="
echo "완료! 이제 ./gradlew run 으로 실행하면"
echo "V1 마이그레이션이 새로 실행됩니다."
echo "========================================="
