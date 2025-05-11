package com.example.CoinWatch.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//User
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val user_id: Int = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String
)