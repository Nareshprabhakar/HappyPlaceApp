package com.example.happyplacesapp.room

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = false)
    val userId:Int = 0,
    @ColumnInfo(name = "UserImage")
    val userImage:String?,
    @ColumnInfo(name = "UserName")
    val userName:String?
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeString(userImage)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserProfileEntity> {
        override fun createFromParcel(parcel: Parcel): UserProfileEntity {
            return UserProfileEntity(parcel)
        }

        override fun newArray(size: Int): Array<UserProfileEntity?> {
            return arrayOfNulls(size)
        }
    }
}