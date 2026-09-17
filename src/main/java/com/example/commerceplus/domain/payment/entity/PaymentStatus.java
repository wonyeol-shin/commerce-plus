package com.example.commerceplus.domain.payment.entity;


// 결제 상태 전이 규칙
// PAYMENT_PENDING → ALL
// COMPLETED       → CANCELED
// FAILED          → (종료)
// CANCELED        → (종료)

//서버가 검증 ,처리를 마친 뒤 저장한 결제 결과
public enum PaymentStatus {

    PAYMENT_PENDING {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == COMPLETED || target == FAILED ||
                    target == CANCEL_FAILED || target == CANCELED;
        }
    },

    COMPLETED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELED;
        }
    },

    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },

    CANCELED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },

    CANCEL_FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            // 운영자가 수동으로 최종 처리한 뒤 종결시킬 수 있게 열어둠
            return target == FAILED || target == CANCELED;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}