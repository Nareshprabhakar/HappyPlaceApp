package com.example.happyplacesapp.room


import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "happy_places")
data class HappyPlaceEntity(
    @PrimaryKey(autoGenerate = true)
     val id:Int = 0,
    @ColumnInfo(name = "Title")
    val title:String?,
    @ColumnInfo(name = "Description")
    val description:String?,
    @ColumnInfo(name = "Date")
    val date:String?,
    @ColumnInfo(name = "Location")
    val location:String?,
    @ColumnInfo(name = "Image")
    val image:String?,

    ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),

    )



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(image)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HappyPlaceEntity> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceEntity {
            return HappyPlaceEntity(parcel)
        }

        override fun newArray(size: Int): Array<HappyPlaceEntity?> {
            return arrayOfNulls(size)
        }
    }
}