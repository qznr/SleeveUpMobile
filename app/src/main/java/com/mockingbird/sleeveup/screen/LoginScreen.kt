package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.ui.theme.*
import com.mockingbird.sleeveup.viewmodel.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DarkPurple
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(id = R.string.welcome_back),
                style = MaterialTheme.typography.displaySmall,
                color = MajorelieBlue
            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = stringResource(id = R.string.login_subtitle),
//                style = MaterialTheme.typography.bodyMedium,
//                color = White
//            )
//            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email_hint), color = White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password_hint), color = White) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White
                )
            )


            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
            ) {
                Text(stringResource(id = R.string.login), color = White)
            }


            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        // Replace with actual Google Icon
//                        imageVector = Icons.Default.Email,
//                        contentDescription = null,
//                        tint = AlmostBlack
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.login_with_google), color = AlmostBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(stringResource(R.string.dont_have_account), color = White)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(stringResource(R.string.register), color = MajorelieBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /*TODO*/ }) {
                Text(stringResource(R.string.forgot_password), color = White)
            }
        }
    }
}