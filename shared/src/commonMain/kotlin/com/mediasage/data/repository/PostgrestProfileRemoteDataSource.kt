package com.mediasage.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

private const val TABLE_NAME = "profiles"

class PostgrestProfileRemoteDataSource(
    private val client: SupabaseClient
) : ProfileRemoteDataSource {

    override suspend fun push(row: ProfileRow) {
        client.postgrest.from(TABLE_NAME).upsert(row)
    }

    override suspend fun fetchOnboarding(userId: String): ProfileOnboardingRow? =
        client.postgrest.from(TABLE_NAME).select {
            filter { eq("user_id", userId) }
        }.decodeSingleOrNull()

    override suspend fun pushOnboarding(row: ProfileOnboardingRow) {
        client.postgrest.from(TABLE_NAME).upsert(row)
    }
}
