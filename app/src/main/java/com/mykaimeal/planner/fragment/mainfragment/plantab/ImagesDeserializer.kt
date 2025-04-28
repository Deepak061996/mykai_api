package com.mykaimeal.planner.fragment.mainfragment.plantab

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.LARGE
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.REGULAR
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.SMALL
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.THUMBNAIL
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.ImagesModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.ThmbnailModel
import java.lang.reflect.Type


class ImagesDeserializer : JsonDeserializer<ImagesModel?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ImagesModel? {
        return try {
            if (json != null && json.isJsonObject) {
                val jsonObject = json.asJsonObject

                ImagesModel(
                    THUMBNAIL = context?.deserialize(jsonObject.get("THUMBNAIL"), ThmbnailModel::class.java),
                    SMALL = context?.deserialize(jsonObject.get("SMALL"), ThmbnailModel::class.java),
                    REGULAR = context?.deserialize(jsonObject.get("REGULAR"), ThmbnailModel::class.java),
                    LARGE = context?.deserialize(jsonObject.get("LARGE"), ThmbnailModel::class.java),
                )

            } else {
                ImagesModel(null, null, null,null)  // If it's not an object, return null fields
            }
        } catch (e: Exception) {
            ImagesModel(null, null, null,null)  // Handle parsing errors gracefully
        }
    }
}


