-- MS-759: account-level onboarding completion
-- Run this once via the Supabase SQL editor before the app's onboarding gate will work.
-- Null (or no profiles row at all) means the reader has not finished onboarding yet.
-- Setting a reader's value back to null makes onboarding show again on their next launch.

alter table profiles add column if not exists onboarding_completed_at timestamptz;

-- Accounts that exist before onboarding ships count as already completed.
update profiles
  set onboarding_completed_at = now()
  where onboarding_completed_at is null;

-- Some existing accounts have no profiles row (signed up before MS-681, or the best-effort
-- profile insert failed). Give them one, already completed, so they skip onboarding too.
insert into profiles (user_id, display_name, onboarding_completed_at)
select u.id, coalesce(u.raw_user_meta_data ->> 'full_name', ''), now()
from auth.users u
where not exists (select 1 from profiles p where p.user_id = u.id);
