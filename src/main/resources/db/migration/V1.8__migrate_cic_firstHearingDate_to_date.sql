update ccd.case_data
set data = jsonb_set(
    data,
    '{firstHearingDate}',
    to_jsonb(
        to_char(
            to_date(data ->> 'firstHearingDate', 'DD Mon YYYY'),
            'YYYY-MM-DD'
        )
    )
)
where case_type_id = 'CriminalInjuriesCompensation'
  and data ->> 'firstHearingDate' ~ '^\d{2} [A-Za-z]{3} \d{4}$';

update ccd.case_data
set data = data - 'firstHearingDate'
where case_type_id = 'CriminalInjuriesCompensation'
  and data ->> 'firstHearingDate' = '';

update ccd.case_data
set data = data - 'firstHearingDate'
where case_type_id = 'CriminalInjuriesCompensation'
  and data ? 'firstHearingDate'
  and data ->> 'firstHearingDate' !~ '^\d{4}-\d{2}-\d{2}$';
