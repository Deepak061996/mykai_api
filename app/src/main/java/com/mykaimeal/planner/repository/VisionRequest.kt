/*
package com.mykameal.planner.repository

// Define Vision API request and response data models
data class VisionRequest(
    val requests: List<Request>
)

data class Request(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String
)

data class Feature(
    val type: String,
    val maxResults: Int
)

data class VisionResponse(
    val responses: List<Response>
)

data class Response(
    val labelAnnotations: List<LabelAnnotation>
)

data class LabelAnnotation(
    val description: String,
    val score: Float
)*/

package com.mykaimeal.planner.repository

// Define Vision API request and response data models
data class VisionRequest(
    val requests: List<Request>
)

data class Request(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String // Base64 image content
)

data class Feature(
    val type: String, // e.g., "WEB_DETECTION"
    val maxResults: Int
)



data class VisionResponse(
    val responses: List<ResponseBody>? = null
)

data class ResponseBody(
    val webDetection: WebDetection? = null,
    val labelAnnotations: List<LabelAnnotation>? = null
)

data class WebDetection(
    val webEntities: List<WebEntity> = emptyList()
)

data class WebEntity(
    val description: String? = null
)

data class LabelAnnotation(
    val description: String? = null
)
