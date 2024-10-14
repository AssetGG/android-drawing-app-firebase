package com.example.dreamdoodler

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filePaths")
class FilePath(
        @PrimaryKey()
        var filePath: String) {
}