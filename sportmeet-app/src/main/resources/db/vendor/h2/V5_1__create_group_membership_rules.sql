-- H2 does not support partial unique indexes, so the membership rules from
-- db/vendor/postgresql/V5_1__create_group_membership_rules.sql are not created here.
-- The application enforces them; their database-level behaviour can only be tested against PostgreSQL.
-- The no-op statement keeps migration versions aligned across database vendors.
SELECT 1;
