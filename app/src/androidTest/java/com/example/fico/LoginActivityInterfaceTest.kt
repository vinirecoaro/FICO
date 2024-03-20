package com.example.fico

import android.support.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fico.presentation.activities.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityInterfaceTest {

    @get:Rule
    var loginActivityRule : ActivityTestRule<LoginActivity> = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun verifyIfEditTextsAreAvailable(){

    }

}