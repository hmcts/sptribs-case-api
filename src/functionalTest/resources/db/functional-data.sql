INSERT INTO ccd.case_data(
    id,
    reference,
    security_classification,
    jurisdiction,
    case_type_id,
    state,
    data
)
VALUES (
    1234567890123456,
    1234567890123456,
    'PUBLIC',
    'ST_CIC',
    'CriminalInjuriesCompensation',
    'Draft',
    '{}'::jsonb
)
ON CONFLICT (reference) DO NOTHING;
