-- Membership rules from the KAN-48 design, enforced by PostgreSQL partial unique indexes.
-- H2 (automated tests) cannot create partial indexes; see db/vendor/h2/V5_1__create_group_membership_rules.sql.
-- IMPORTANT: Do not modify this migration once it has been applied to a shared database.

-- A user has at most one active membership in a group.
CREATE UNIQUE INDEX UQ_T_GROUP_MEMBERSHIP_ACTIVE ON T_GROUP_MEMBERSHIP (GROUP_ID, USER_ID)
    WHERE STATUS = 'ACTIVE' AND VALID;

-- A group has at most one active primary owner.
-- "At least one" cannot be expressed by an index; the application creates the owner together with the group.
CREATE UNIQUE INDEX UQ_T_GROUP_MEMBERSHIP_OWNER ON T_GROUP_MEMBERSHIP (GROUP_ID)
    WHERE ROLE = 'OWNER' AND STATUS = 'ACTIVE' AND VALID;
