package com.example.datatransfer

data class User(
    var username: String,
    var email: String,
    var phone_number: String,
    var date_registered: String,
    var expiry_date: String,
    var firebase_tokens: String,
    var user_password: String,
    var logged_in: String,
    var active: String,
    var app_version_ios: String,
    var app_version_android: String,
    var device: String,
    var ip_address: String,
    var profile_image_url: String,
    var hash: String
)
