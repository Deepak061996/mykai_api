package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.mykaimeal.planner.R
import com.skydoves.powerspinner.PowerSpinnerView
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.BankAccountTokenParams
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.MonthYearsCardAdapter
import com.mykaimeal.planner.adapter.MyWalletAdapter
import com.mykaimeal.planner.adapter.MyWalletBankAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.CompressImage
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentPaymentMethodBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.WalletViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard.CardData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard.CradApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecountry.CountryResponseModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsetransfer.TransferModel
import com.mykaimeal.planner.listener.CardBankListener
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Calendar
import java.util.Objects

@AndroidEntryPoint
class PaymentMethodFragment : Fragment(), CardBankListener {

    private lateinit var binding: FragmentPaymentMethodBinding
    private lateinit var viewModel: WalletViewModel
    private var stripe: Stripe? = null
    private var month: Int = 0
    private var year: Int = 0
    private lateinit var adapterCard: MyWalletAdapter
    private lateinit var adapterCardBank: MyWalletBankAdapter
    private var dataLocal: MutableList<CardData> = mutableListOf()
    private var bankDataLocal: MutableList<CardData> = mutableListOf()
    private var storage_permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private var storage_permissions_33 =
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
    private val mediaFiles: ArrayList<MediaFile?> = ArrayList()
    private val REQUEST_Folder = 2
    private var imgtype: String = ""
    private var filefrontid: String = "No"
    private var filebackid: String = "No"
    private var filebankid: String = "No"
    private var filefront: File? = null
    private var fileback: File? = null
    private var bankuploadfile: File? = null
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private var country: String = ""
    private var states: String = ""
    private var currency: String = ""
    private var amount: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentMethodBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[WalletViewModel::class.java]
        stripe = Stripe(requireContext(), getString(R.string.publish_key))

        (activity as MainActivity).binding.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        amount = arguments?.getString("amount", "$ 0")?:"$ 0"

        ActivityCompat.requestPermissions(requireActivity(), permissions(), REQUEST_CODE_STORAGE_PERMISSION)


        setupBackNavigation()

        setupUI()

        // Add Refresh Api
        // Set up swipe-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Perform your refresh operation
            fetchDataOnLoad()
        }


        // Add Card Api Event
        setUpAddCardEvent()

        // Add Bank Api Event
        setUpBankEvent()

        setupSpinners()

        // When screen load then api call
        fetchDataOnLoad()

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun fetchStates(value: String) {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                viewModel.countryStateCityRequest({
                    BaseApplication.dismissMe()
                    handleApiStatesResponse(it, value)
                }, "https://api.countrystatecity.in/v1/countries/$value/states/")
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchCity(value: String, iso2: String) {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                viewModel.countryStateCityRequest({
                    BaseApplication.dismissMe()
                    handleApiCitiesResponse(it)
                }, "https://api.countrystatecity.in/v1/countries/$value/states/$iso2/cities/")
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun handleApiStatesResponse(result: NetworkResult<String>, value: String) {
        when (result) {
            is NetworkResult.Success -> handleStatesSuccessResponse(result.data.toString(), value)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiCitiesResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleCitiesSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    private fun fetchCountry() {
        if (BaseApplication.isOnline(requireActivity())) {
            lifecycleScope.launch {
                viewModel.countryStateCityRequest({
                    BaseApplication.dismissMe()
                    handleApiCountryResponse(it)
                }, "https://api.countrystatecity.in/v1/countries/")
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun setupSpinners() {
        setupSpinner(binding.spinnerSelectIDType, listOf("Driver license", "Passport"))
        setupSpinner(
            binding.spinnerSelectOption,
            listOf("Bank account statement", "Voided cheque", "Bank letterhead")
        )
    }

    private fun setupSpinner(spinner: PowerSpinnerView, items: List<String>) {
        spinner.setItems(items)
        spinner.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) spinner.show() else spinner.dismiss()
        }
        spinner.setIsFocusable(true)
    }


    private fun setUpBankEvent() {
        binding.textAddBank.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation()) {
                    addBankApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun addBankApi() {
        BaseApplication.showMe(requireContext())
        val bankAccountTokenParams = BankAccountTokenParams(
            country, currency,
            binding.etBankAccountNumber.text?.toString()?.trim()
                ?: throw IllegalArgumentException("Bank number is required"),
            BankAccountTokenParams.Type.Individual,
            binding.etAccountHolderName.text.toString(),
            binding.etRoutingNumber.text?.toString()?.trim()
                ?: throw IllegalArgumentException("Routing number is required")
        )


        stripe?.createBankAccountToken(
            bankAccountTokenParams,
            null,
            null,
            object : ApiResultCallback<Token> {
                override fun onSuccess(token: Token) {
                    lifecycleScope.launch {
                        addCard(token)
                    }
                }

                override fun onError(e: Exception) {
                    BaseApplication.dismissMe()
                    showAlert(e.message, false)
                }
            })


    }

    private suspend fun addCard(token: Token) {

        try {
            val filePartFront: MultipartBody.Part? = if (filefront != null) {
                val requestBody =
                    filefront?.asRequestBody(filefront!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("document_front", filefront?.name, requestBody!!)
            } else {
                null
            }
            val filePartBack: MultipartBody.Part? = if (fileback != null) {
                val requestBody = fileback?.asRequestBody(fileback!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("document_back", fileback?.name, requestBody!!)
            } else {
                null
            }

            val filePart: MultipartBody.Part? = if (bankuploadfile != null) {
                val requestBody =
                    bankuploadfile?.asRequestBody(bankuploadfile!!.extension.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("bank_proof", bankuploadfile?.name, requestBody!!)
            } else {
                null
            }

            val firstNameBody = binding.etFirstName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val lastNameBody = binding.etLastName.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val emailBody = binding.etEmail.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val phoneBody = binding.etPhoneNumber.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val dobBody = binding.etDOB.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val idTypeBody = binding.spinnerSelectIDType.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val personalIdentificationNobody =
                binding.etPersonalIdentificationNumber.text.toString().trim()
                    .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val ssnBody = binding.etSSN.text.toString().trim()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val addressBody = binding.etAddress.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val countryBody = country.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val shortStateNameBody = states.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val cityBody = binding.spinnerSelectCity.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val postalCodeBody = binding.etPostalCode.text.toString()
                .toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val bankDocumentTypeBody = when (binding.spinnerSelectOption.getText().toString()) {
                "Bank account statement" -> "statement".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                "Voided cheque" -> "cheque".toRequestBody("multipart/form-data".toMediaTypeOrNull())
                else -> "letterhead".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            }
            val deviceTypeBody = "Android".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val tokenTypeBody =
                "bank_account".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val stripeTokenBody = token.id.toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val saveCardBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val amountBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val paymentTypeBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val bankIdBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())

            viewModel.addBankRequest({
                BaseApplication.dismissMe()
                handleApiBankResponse(it)
            },
                filePartFront,
                filePartBack,
                filePart,
                firstNameBody,
                lastNameBody,
                emailBody,
                phoneBody,
                dobBody,
                personalIdentificationNobody,
                idTypeBody,
                ssnBody,
                addressBody,
                countryBody,
                shortStateNameBody,
                cityBody,
                postalCodeBody,
                bankDocumentTypeBody,
                deviceTypeBody,
                tokenTypeBody,
                stripeTokenBody,
                saveCardBody,
                amountBody,
                paymentTypeBody,
                bankIdBody)

        } catch (e: Exception) {
            BaseApplication.dismissMe()
            showAlert(e.message, false)
        }

    }

    private fun handleApiBankResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleBankSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun isValidation(): Boolean {
        if (binding.etFirstName.text.trim().toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.firstNameError, false)
            return false
        } else if (binding.etLastName.text?.trim().toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.lastNameError, false)
            return false
        } else if (binding.etEmail.text?.trim().toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.emailError, false)
            return false
        } else if (!binding.etEmail.text?.trim().toString().contains("@")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.validEmail, false)
            return false
        } else if (binding.etPhoneNumber.text?.trim().toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.phoneError, false)
            return false
        } else if (binding.etPhoneNumber.text?.trim().toString().length != 10) {
            BaseApplication.alertError(requireContext(), ErrorMessage.validPhoneNumber, false)
            return false
        } else if (binding.etDOB.text?.toString().equals("MM/DD/YYYY")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.dobError, false)
            return false
        } else if (binding.spinnerSelectIDType.text?.toString().equals("Select ID type")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.selectIdTypeError, false)
            return false
        } else if (binding.etPersonalIdentificationNumber.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.pINError, false)
            return false
        } else if (binding.etSSN.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.SNNError, false)
            return false
        } else if (binding.etSSN.text?.trim().toString().length != 4) {
            BaseApplication.alertError(requireContext(), ErrorMessage.SNNValidError, false)
            return false
        } else if (binding.etAddress.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.addressError, false)
            return false
        } else if (binding.spinnerSelectCountry.text?.toString().equals("Select Country")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.countryError, false)
            return false
        } else if (binding.spinnerSelectState.text?.toString().equals("Select State")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.stateError, false)
            return false
        } else if (binding.spinnerSelectCity.text?.toString().equals("Select City")) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cityError, false)
            return false
        } else if (binding.etPostalCode.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.postalCodeError, false)
            return false
        } else if (binding.etBankName.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.bankNameError, false)
            return false
        } else if (binding.etAccountHolderName.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cardholderError, false)
            return false
        } else if (binding.etBankAccountNumber.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.accountNumberError, false)
            return false
        } else if (binding.etConfirmAccountNumber.text?.trim().toString().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cAccountNumberError, false)
            return false
        } else if (binding.etRoutingNumber.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.routingNumberError, false)
            return false
        } else if (filebankid.equals("No", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.proofofbanError, false)
            return false
        } else if (filefrontid.equals("No", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.frontimageError, false)
            return false
        } else if (filebackid.equals("No", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.backimageError, false)
            return false
        }

        return true
    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchUserBankAndCardData()
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchUserBankAndCardData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.getCardAndBankRequest { result ->
//                BaseApplication.dismissMe()
                binding.swipeRefreshLayout.isRefreshing = false
                handleApiResponse(result)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> processSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
        // When screen load then api call
        fetchCountry()
    }

    private fun processSuccessResponse(response: String) {
        try {
            val apiModel = Gson().fromJson(response, CradApiResponse::class.java)
            Log.d("@@@ Response cardBank ", "message :- $response")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data?.let { updateUI(it) } ?: run {
                    // Code for the else condition
                    binding.llBankAccount.visibility = View.VISIBLE
                    binding.llSavedBankAccountDetails4.visibility = View.GONE
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    private fun updateUI(data: Data) {

        dataLocal.clear()
        bankDataLocal.clear()
        if (data.card_details != null) {
            dataLocal.addAll(data.card_details)
        }
        if (data.bank_details != null) {
            for (dataLoop in data.bank_details) {
                // Change value cardId to currency
                bankDataLocal.add(
                    CardData(
                        dataLoop.verification_status.toString(),
                        dataLoop.bank_account[0].currency.uppercase(),
                        0,
                        0,
                        dataLoop.bank_account[0].account_holder_name + "," + dataLoop.bank_account[0].last4 + "(" + dataLoop.bank_account[0].currency.uppercase() + ")",
                        dataLoop.bank_account[0].bank_name,
                        dataLoop.bank_account[0].account.toString()
                    )
                )
            }
        }

        if (dataLocal.size > 0 || bankDataLocal.size > 0) {
            binding.llBankAccount.visibility = View.GONE
            binding.llSavedBankAccountDetails4.visibility = View.VISIBLE

            if (dataLocal.size > 0) {
                adapterCard = MyWalletAdapter(requireContext(), dataLocal, "card", this)
                binding.rcvCardNumber.adapter = adapterCard
            }

            if (bankDataLocal.size > 0) {
                adapterCardBank = MyWalletBankAdapter(requireContext(), bankDataLocal, "bank", this)
                binding.rcvBankAccounts.adapter = adapterCardBank
            }


        } else {
            binding.llBankAccount.visibility = View.VISIBLE
            binding.llSavedBankAccountDetails4.visibility = View.GONE
//            fetchDataOnLoad()
        }


    }


    private fun setupUI() {

        binding.imgWallet.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.textAddCardNumber.setOnClickListener {
            binding.llBankAccount.visibility = View.VISIBLE
            binding.llSavedBankAccountDetails4.visibility = View.GONE
            toggleBankAndCardView(true)
        }

        binding.etMonth.setOnClickListener {
            showPopupMonth()
        }

        binding.etYear.setOnClickListener {
            showPopupYears()
        }

        binding.textBankAccountToggle.setOnClickListener {
            toggleBankAndCardView(true)
        }

        binding.textDebitCardToggle.setOnClickListener {
            toggleBankAndCardView(false)
        }

        binding.rlDOB.setOnClickListener {
            openCalendarBox()
        }

        binding.imguploaddocument.setOnClickListener {

            if (hasPermissions(requireContext(), *permissions())) {
                val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
                dialog.setContentView(R.layout.alert_box_gallery_pdf)
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.window!!.attributes)
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.window!!.attributes = layoutParams
                val laygallery: LinearLayout = dialog.findViewById(R.id.lay_gallery)
                val laycamera: LinearLayout = dialog.findViewById(R.id.lay_camera)
                val view1: View = dialog.findViewById(R.id.view1)
                val laypdf: LinearLayout = dialog.findViewById(R.id.lay_pdf)
                view1.visibility = View.VISIBLE
                laycamera.visibility = View.VISIBLE
                laycamera.setOnClickListener {
                    dialog.dismiss()
                    imgtype = "camera"
                    ImagePicker.with(this)
                        .cameraOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start()
                }

                laygallery.setOnClickListener {
                    dialog.dismiss()
                    imgtype = "Gallery"
                    ImagePicker.with(this)
                        .galleryOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start()
                }

                laypdf.setOnClickListener {
                    imgtype = "pdffile"
                    dialog.dismiss()
                    fileIntentMulti()
                }

                dialog.show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please go to setting Enable Permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.layFront.setOnClickListener {
            imgtype = "front"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.layBack.setOnClickListener {
            imgtype = "back"
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
    }

    private fun fileIntentMulti() {
        val intent = Intent(requireContext(), FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setSelectedMediaFiles(mediaFiles)
                .setShowFiles(true)
                .setShowImages(false)
                .setShowAudios(false)
                .setShowVideos(false)
                .setIgnoreNoMedia(false)
                .enableVideoCapture(false)
                .enableImageCapture(false)
                .setIgnoreHiddenFile(false)
                .setMaxSelection(1)
                .build()
        )
        startActivityForResult(intent, REQUEST_Folder)
    }


    @SuppressLint("MissingInflatedId")
    private fun showPopupMonth() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_month_years, null)
        val popupWindow = PopupWindow(popupView, binding.etMonth.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(binding.etMonth, 0, 0, Gravity.CENTER)
        val monthList= mutableListOf("01","02","03","04","05","06","07","08","09","10","11","12")
        // Access views inside the inflated layout using findViewById
        val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcyData)
        val adapterMonth= MonthYearsCardAdapter(monthList,requireContext()){
                data->
            month=data.toInt()
            binding.etMonth.text=data
            popupWindow.dismiss()
        }
        rcyData?.adapter=adapterMonth
    }

    @SuppressLint("MissingInflatedId")
    private fun showPopupYears() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_month_years, null)
        val popupWindow = PopupWindow(popupView, binding.etYear.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(binding.etYear, 0, 0, Gravity.CENTER)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearList = (currentYear..currentYear + 29).map { it.toString() }
        yearList as MutableList
        // Access views inside the inflated layout using findViewById
        val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcyData)
        val adapterMonth= MonthYearsCardAdapter(yearList ,requireContext()){
                data->
            year=data.toInt()
            binding.etYear.text=data
            popupWindow.dismiss()
        }
        rcyData?.adapter=adapterMonth
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagePicker.REQUEST_CODE) {
            if (data?.data != null) {
                if (imgtype.equals("front", true)) {
                    val uri = data.data!!
                    filefront = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filefrontid = "Yes"
                    binding.textChooseVerificationDocument.text = filefront.toString()

                }
                if (imgtype.equals("back", true)) {
                    val uri = data.data!!
                    fileback = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filebackid = "Yes"
                    binding.textChooseVerificationDocumentBack.text = fileback.toString()

                }

                if (imgtype.equals("camera", true)) {
                    val uri = data.data!!
                    bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    binding.textChooseBankProof.text = bankuploadfile.toString()
                    filebankid = "Yes"

                }
                if (imgtype.equals("Gallery", true)) {
                    val uri = data.data!!
                    bankuploadfile = BaseApplication.getPath(requireContext(), uri)?.let { File(it) }
                    filebankid = "Yes"
                    binding.textChooseBankProof.text = bankuploadfile.toString()

                }
            }
        }
        if (requestCode == REQUEST_Folder) {
            data?.let { onSelectFromFolderResult(it) }
        }
    }

    private fun onSelectFromFolderResult(data: Intent?) {
        if (data != null) {
            try {
                val files =
                    data.getParcelableArrayListExtra<MediaFile>(FilePickerActivity.MEDIA_FILES)
                Log.v("pdf", files!![0].uri.toString())
                Log.v("pdf", files[0].name.toString())
                bankuploadfile = CompressImage.from(requireContext(), files[0].uri)
                filebankid = "Yes"
                binding.textChooseBankProof.text = bankuploadfile.toString()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.d("pdf not found", "no data :-" + e.message.toString())
            }
        }
    }

    private fun permissions(): Array<String> {
        val p: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        return p
    }


    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }


    // This function is use for open the Calendar
    @SuppressLint("SetTextI18n")
    private fun openCalendarBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()

        // Extract the current year, month, and day
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog with the current date and minimum date set to today
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                val date = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                Log.d("******", "" + date)
                binding.etDOB.text = BaseApplication.changeDateFormat(date)
            },
            year,
            month,
            day
        )

        // Disable previous dates
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        // Show the date picker dialog
        datePickerDialog.show()
    }


    private fun toggleBankAndCardView(showBank: Boolean) {
        binding.textBankAccountToggle.setBackgroundResource(
            if (showBank) R.drawable.selected_green_toogle_bg else 0
        )
        binding.textDebitCardToggle.setBackgroundResource(
            if (!showBank) R.drawable.selected_green_toogle_bg else 0
        )
        binding.textBankAccountToggle.setTextColor(
            Color.parseColor(if (showBank) "#FFFFFF" else "#06C169")
        )
        binding.textDebitCardToggle.setTextColor(
            Color.parseColor(if (!showBank) "#FFFFFF" else "#06C169")
        )
        binding.cvBankAccount2.visibility = if (showBank) View.VISIBLE else View.GONE
        binding.cvDebitCard3.visibility = if (showBank) View.GONE else View.VISIBLE
    }


    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun openYearPickerBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()
        // Extract the current year and month
        val currentYear = calendar.get(Calendar.YEAR)
        // Create a dialog
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_picker, null)
        // Get references to the NumberPickers in the custom dialog layout
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.Picker)
        // Configure the year picker
        monthPicker.minValue = currentYear
        monthPicker.maxValue = currentYear + 50 // Limit to 50 years ahead
        monthPicker.value = currentYear
        // Set the custom view in the dialog
        dialog.setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                // Get selected month and year
                val selectedMonth = monthPicker.value
                year = monthPicker.value
                // Update the TextView with the selected month name and year
                binding.etYear.text = "" + selectedMonth
            }
            .setNegativeButton("Cancel", null)
        // Show the dialog
        dialog.create().show()
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun openMonthPickerBox() {
        // Get the current calendar instance
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        // Array of month names
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        // Create a dialog
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_picker, null)
        // Get references to the NumberPickers in the custom dialog layout
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.Picker)
        // Configure the month picker
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = monthNames
        monthPicker.value = currentMonth
        // Set the custom view in the dialog
        dialog.setView(dialogView)
            .setPositiveButton("OK") { _, _ ->

                val selectedMonth = monthPicker.value
                month = monthPicker.value + 1
                // Update the TextView with the selected month name and year
                binding.etMonth.text = monthNames[selectedMonth]
                Toast.makeText(
                    requireContext(),
                    "selectedMonth :- $selectedMonth",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)

        // Show the dialog
        dialog.create().show()
    }


    private fun setUpAddCardEvent() {

        binding.textAddCardDebitCard.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidationCard()) {
                    paymentApi()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun paymentApi() {
        BaseApplication.showMe(requireContext())
        val cardNumber: String = Objects.requireNonNull(binding.etCardNumber.text.toString()).toString()
        val cvvNumber: String = Objects.requireNonNull(binding.etCVVNumber.text.toString()).toString()
        val name: String = binding.etName.text.toString()
        val card = CardParams(cardNumber, Integer.valueOf(month), Integer.valueOf(year), cvvNumber, name)
        stripe!!.createCardToken(card, null, null, object : ApiResultCallback<Token> {
            override fun onError(e: Exception) {
                BaseApplication.dismissMe()
                Log.d("PaymentActivity1", "data$e")
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(result: Token) {
                val id = result.id
                Log.d("@@@Token:-", "data$id")
                saveCardApi(id)
            }
        })
    }

    private fun saveCardApi(id: String) {
        lifecycleScope.launch {
            viewModel.addCardRequest({
                BaseApplication.dismissMe()
                handleApiUpdateResponse(it)
            }, id)
        }
    }

    private fun handleApiUpdateResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiCountryResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleCountrySuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleApiTransferResponse(result: NetworkResult<String>, dialog: Dialog?) {
        when (result) {
            is NetworkResult.Success -> handleTransferSuccessResponse(result.data.toString(), dialog)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    private fun handleApiDeleteCardResponse(
        result: NetworkResult<String>,
        position: Int?,
        type: String
    ) {
        when (result) {
            is NetworkResult.Success -> handleDeleteCardSuccessResponse(
                result.data.toString(),
                position,
                type
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdateSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {

                month = 0
                year = 0
                binding.etName.text.clear()
                binding.etCardNumber.text.clear()
                binding.etCVVNumber.text.clear()
                binding.etMonth.text = "Month"
                binding.etYear.text = "Year"

                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()

                // When screen load then api call
                fetchDataOnLoad()

            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleCountrySuccessResponse(data: String) {
        try {
            val apiModelCountry = Gson().fromJson(data, CountryResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModelCountry.code == 200 && apiModelCountry.success) {
                if (apiModelCountry.data != null) {
                    val localList: MutableList<String> = mutableListOf()
                    localList.clear()
                    for (value in apiModelCountry.data) {
                        localList.add(value.name)
                    }
                    binding.spinnerSelectCountry.setItems(localList)
                    binding.spinnerSelectCountry.setIsFocusable(true)
                    binding.spinnerSelectCountry.setOnSpinnerItemSelectedListener<String> { _, _, newIndex, _ ->
                        country = apiModelCountry.data[newIndex].iso2
                        currency = apiModelCountry.data[newIndex].currency
                        // When screen load then api call
                        fetchStates(country)
                    }

                }
            } else {
                if (apiModelCountry.code == ErrorMessage.code) {
                    showAlert(apiModelCountry.message, true)
                } else {
                    showAlert(apiModelCountry.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }


    @SuppressLint("SetTextI18n")
    private fun handleTransferSuccessResponse(data: String, dialog: Dialog?) {
        try {
            val apiModelCountry = Gson().fromJson(data, TransferModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            // api response change deepak
            if (/*apiModelCountry.code == 200 &&*/ apiModelCountry.success) {
                /*if (apiModelCountry.data != null) {
                    dialog?.dismiss()
                    Toast.makeText(requireContext(),apiModelCountry.message,Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }*/
                dialog?.dismiss()
                Toast.makeText(requireContext(),apiModelCountry.message,Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                if (apiModelCountry.code == ErrorMessage.code) {
                    showAlert(apiModelCountry.message, true)
                } else {
                    showAlert(apiModelCountry.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleStatesSuccessResponse(data: String, value: String) {
        try {
            val apiModelCountry = Gson().fromJson(data, CountryResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModelCountry.code == 200 && apiModelCountry.success) {
                if (apiModelCountry.data != null) {
                    val localList: MutableList<String> = mutableListOf()
                    localList.clear()
                    for (value in apiModelCountry.data) {
                        localList.add(value.name)
                    }
                    binding.spinnerSelectState.setItems(localList)
                    binding.spinnerSelectState.setIsFocusable(true)
                    binding.spinnerSelectState.setOnSpinnerItemSelectedListener<String> { _, _, newIndex, _ ->
                        states = apiModelCountry.data[newIndex].iso2
                        // When screen load then api call
                        fetchCity(value, states)
                    }

                }
            } else {
                if (apiModelCountry.code == ErrorMessage.code) {
                    showAlert(apiModelCountry.message, true)
                } else {
                    showAlert(apiModelCountry.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleCitiesSuccessResponse(data: String) {
        try {
            val apiModelCountry = Gson().fromJson(data, CountryResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModelCountry.code == 200 && apiModelCountry.success) {
                if (apiModelCountry.data != null) {
                    val localList: MutableList<String> = mutableListOf()
                    localList.clear()
                    for (value in apiModelCountry.data) {
                        localList.add(value.name)
                    }
                    binding.spinnerSelectCity.setItems(localList)
                    binding.spinnerSelectCity.setIsFocusable(true)
                }
            } else {
                if (apiModelCountry.code == ErrorMessage.code) {
                    showAlert(apiModelCountry.message, true)
                } else {
                    showAlert(apiModelCountry.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun handleBankSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {

                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                binding.etFirstName.text.clear()
                binding.etLastName.text.clear()
                binding.etEmail.text.clear()
                binding.etPhoneNumber.text.clear()
                binding.etDOB.text = "MM/DD/YYYY"
                binding.spinnerSelectIDType.text = "Select ID type"
                binding.etPersonalIdentificationNumber.text.clear()
                binding.etSSN.text.clear()
                binding.etAddress.text.clear()
                binding.spinnerSelectCountry.text = "Select Country"
                binding.spinnerSelectState.text = "Select State"
                binding.spinnerSelectCity.text = "Select City"
                binding.etPostalCode.text.clear()
                binding.etBankName.text.clear()
                binding.etAccountHolderName.text.clear()
                binding.etBankAccountNumber.text.clear()
                binding.etConfirmAccountNumber.text.clear()
                binding.etRoutingNumber.text.clear()
                binding.textChooseBankProof.text = "Choose a file"
                binding.textChooseVerificationDocument.text = "Choose a file"
                binding.textChooseVerificationDocumentBack.text = "Choose a file"
                filebankid = "No"
                filefrontid = "No"
                filebackid = "No"
                filefront = null
                fileback = null
                bankuploadfile = null
                // When screen load then api call
                fetchDataOnLoad()
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }


    private fun handleDeleteCardSuccessResponse(data: String, position: Int?, type: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Add Card", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {

                if (type.equals("Bank", true)) {
                    bankDataLocal.removeAt(position!!)
                } else {
                    dataLocal.removeAt(position!!)
                }

                if (dataLocal.size > 0 || bankDataLocal.size > 0) {


                    if (bankDataLocal.size > 0) {
                        adapterCardBank.upDateList(bankDataLocal, "bank")
                        binding.rcvBankAccounts.adapter = adapterCardBank
                        binding.rcvBankAccounts.visibility = View.VISIBLE
                    } else {
                        binding.rcvBankAccounts.visibility = View.GONE
                    }

                    if (dataLocal.size > 0) {
                        adapterCard.upDateList(dataLocal, "card")
                        binding.rcvCardNumber.adapter = adapterCard
                        binding.rcvCardNumber.visibility = View.VISIBLE
                    } else {
                        binding.rcvCardNumber.visibility = View.GONE
                    }

                    binding.llBankAccount.visibility = View.GONE
                    binding.llSavedBankAccountDetails4.visibility = View.VISIBLE


                } else {
                    binding.llBankAccount.visibility = View.VISIBLE
                    binding.llSavedBankAccountDetails4.visibility = View.GONE
                }
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }

    }

    private fun isValidationCard(): Boolean {
        if (binding.etName.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cardholderError, false)
            return false
        } else if (binding.etCardNumber.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cardNumberError, false)
            return false
        } else if (binding.etCVVNumber.text.toString().trim().isEmpty()) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cvvError, false)
            return false
        } else if (binding.etCVVNumber.text.toString().length == 1 || binding.etCVVNumber.text.toString().length == 2) {
            BaseApplication.alertError(requireContext(), ErrorMessage.cvvValidError, false)
            return false
        } else if (binding.etMonth.text.toString().equals("Month", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.monthError, false)
            return false
        } else if (binding.etYear.text.toString().equals("Year", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.yearError, false)
            return false
        }

        return true
    }


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
           viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        if (status!!.equals("card", true)) {
            if (type!!.equals("delete", true)) {
                if (BaseApplication.isOnline(requireActivity())) {
                    deleteApi(position, status)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }/*else{
                 showWithdrawAmountDialog(position)
             }*/

        }

        if (status.equals("Bank", true)) {
            if (type!!.equals("delete", true)) {
                if (BaseApplication.isOnline(requireActivity())) {
                    deleteBankApi(position, status)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            } else {
                if (bankDataLocal[position!!].brand.toString().equals("true", true)) {
                    showWithdrawAmountDialog(position)
                } else {
                    showAlert(ErrorMessage.bankVerifyError, false)
                }
            }

        }

    }


    private fun deleteApi(position: Int?, type: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.deleteCardRequest({
                BaseApplication.dismissMe()
                handleApiDeleteCardResponse(it, position, type)
            }, dataLocal[position!!].card_id.toString(), dataLocal[position].customer_id.toString())
        }
    }

    private fun deleteBankApi(position: Int?, type: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.deleteBankRequest({
                BaseApplication.dismissMe()
                handleApiDeleteCardResponse(it, position, type)
            }, bankDataLocal[position!!].customer_id!!)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showWithdrawAmountDialog(position: Int?) {

        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog).apply {}
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog_withdraw_amount)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window?.attributes)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }


        val etWithdrawalAmount = dialog.findViewById<EditText>(R.id.etWithdrawalAmount)

        dialog.findViewById<ImageView>(R.id.imageCross).setOnClickListener {
            dialog.dismiss()
        }

        if (amount != null) {
            dialog.findViewById<TextView>(R.id.textAvailableBalance).text =
                "Available Balance :$amount"
        }


        dialog.findViewById<RelativeLayout>(R.id.rlWithdrawAmountButton).setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                val localAmount = amount?.replace("$", "")?.trim()?.toDouble()
                val userEnteredAmount = etWithdrawalAmount.text.toString()

                if (userEnteredAmount.trim().isEmpty()) {
                    BaseApplication.alertError(
                        requireContext(),
                        ErrorMessage.amountEmptyError,
                        false
                    )
                } else {
                    if (localAmount != null) {
                        if (localAmount <= userEnteredAmount.toDouble()) {
                            BaseApplication.alertError(
                                requireContext(),
                                ErrorMessage.amounthightError,
                                false
                            )
                        } else if (userEnteredAmount.toDouble() == 0.0) {
                            // Handle the case where the local amount is lower than the entered amount
                            BaseApplication.alertError(
                                requireContext(),
                                ErrorMessage.amountlowError,
                                false
                            )
                        } else {
                            // Amounts are equal; proceed with the transfer
                            transferAmount(
                                dialog,
                                bankDataLocal[position!!].customer_id,
                                localAmount.toString()
                            )
                        }
                    } else {
                        // Handle the case where localAmount is null
                        BaseApplication.alertError(
                            requireContext(),
                            ErrorMessage.amountError,
                            false
                        )
                    }
                }


            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        dialog.show()
    }

    private fun transferAmount(dialog: Dialog?, customerId: String?, amount: String?) {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                viewModel.transferAmountRequest({
                    BaseApplication.dismissMe()
                    handleApiTransferResponse(it, dialog)
                }, amount!!, customerId!!)
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


}