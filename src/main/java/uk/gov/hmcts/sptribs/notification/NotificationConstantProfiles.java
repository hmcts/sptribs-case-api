package uk.gov.hmcts.sptribs.notification;

import uk.gov.hmcts.sptribs.notification.model.NotificationContext;
import uk.gov.hmcts.sptribs.notification.model.NotificationContextRequest;


public enum NotificationConstantProfiles {

    ANONYMITY_APPLIED {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },

    BUNDLE_CREATED {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    CANCEL_HEARING {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    CASE_FLAG {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .beforeData(request.getPreviousCaseData())
                .notification(request.getNotification())
                .build();
        }
    },
    CIC_SUBMIT_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    CLOSE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    CONTACT_PARTIES {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .uploadedDocuments(request.getUploadedDocuments())
                .build();
        }
    },

    CREATE_AND_SEND_ORDER {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    CREATE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },

    DECISION_ISSUED {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    DSS_UPDATE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    EDIT_RECORD_LISTING {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    ISSUE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    ISSUE_FINAL_DECISION {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    LINK_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    MAINTAIN_LINK {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    POSTPONE_HEARING {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    RECORD_LISTING {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    REINSTATE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    SEND_ORDER {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    STAY_THE_CASE {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    },
    UPDATE_ANONYMITY {
        @Override
        public NotificationContext buildContext(NotificationContextRequest request) {
            return NotificationContext.builder()
                .caseData(request.getCaseData())
                .caseReference(request.getCaseReference())
                .correspondenceParties(request.getNotification().buildCorrespondenceParties(request))
                .notification(request.getNotification())
                .build();
        }
    };

public abstract NotificationContext buildContext(NotificationContextRequest request);
}
