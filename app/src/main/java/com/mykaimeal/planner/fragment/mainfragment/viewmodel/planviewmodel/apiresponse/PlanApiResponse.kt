import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse.Data

data class PlanApiResponse(
    val code: Int,
    val `data`: Data?,
    val message: String,
    val success: Boolean
)