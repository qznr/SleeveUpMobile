package com.mockingbird.sleeveup.retrofit

import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("companies.json")
    suspend fun getCompanies(): Map<String, Company>

    @GET("job_offers.json")
    suspend fun getJobOffers(): Map<String, JobOffer>

    @GET("companies/{companyId}.json")
    suspend fun getCompanyById(@Path("companyId") companyId: String): Company

    @GET("job_offers/{jobOfferId}.json")
    suspend fun getJobOfferById(@Path("jobOfferId") jobOfferId: String): JobOffer

    @GET("job_offers.json")
    suspend fun getJobOffersByCompanyId(
        @Query("orderBy") orderBy: String = "\"company_id\"",
        @Query("equalTo") companyId: String
    ): Map<String, JobOffer>
}