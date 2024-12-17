package com.mockingbird.sleeveup.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import com.mockingbird.sleeveup.entity.Event
import com.mockingbird.sleeveup.navigation.Screen

@Composable
fun CompanyCard(
    companyId: String,
    company: Company,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    jobOffers: Map<String, JobOffer>,
    user: User?,
    navController: NavController
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable { onCardClick() }) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = companyId,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    if (jobOffers.isEmpty()) {
                        Text(text = "No jobs offers for this company yet!")
                    } else {
                        jobOffers.toList().forEach { (jobOfferId, jobOffer) ->
                            JobOfferCard(
                                jobOfferId = jobOfferId,
                                jobOffer = jobOffer,
                                navController = navController
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun JobOfferCard(
    jobOfferId: String, jobOffer: JobOffer, navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = jobOffer.profession,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = jobOffer.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                navController.navigate(Screen.JobDetails.createRoute(jobOfferId))
            }) {
                Text("See job details")
            }
        }
    }
}

@Composable
fun EventCard(event: Event, eventId: String, navController: NavController) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = eventId) // Or whatever you want to show about the id
        }
    }
}