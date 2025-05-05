package com.mykaimeal.planner.fragment.mainfragment.commonscreen.supermarktesnearbyscreen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterSuperMarket
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentSuperMarketsNearByBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.viewmodel.BasketDetailsSuperMarketViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SuperMarketsNearByFragment : Fragment(), OnItemSelectUnSelectListener, OnMapReadyCallback {

    private lateinit var binding: FragmentSuperMarketsNearByBinding
    private var adapter: AdapterSuperMarket? = null
    private lateinit var basketDetailsSuperMarketViewModel: BasketDetailsSuperMarketViewModel
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    private var storeUid: String? = ""
    private var storeName: String? = ""
    private var currentPage:Int=1
    var isUserScrolling = false
    var isLoading = false
    private var hasMoreData = true
    private var stores: MutableList<Store> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSuperMarketsNearByBinding.inflate(layoutInflater, container, false)

        basketDetailsSuperMarketViewModel =
            ViewModelProvider(requireActivity())[BasketDetailsSuperMarketViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        adapter = AdapterSuperMarket(stores, requireActivity(), this)


        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialize()

        return binding.root
    }

    private fun initialize() {

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.textDelivery.setOnClickListener {
            updateButtonStyles(binding.textDelivery, binding.textCollect)
        }

        binding.textCollect.setOnClickListener {
            updateButtonStyles(binding.textCollect, binding.textDelivery)
        }



        // Scroll listener for pagination
        binding.recySuperMarket.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isUserScrolling || isLoading || !hasMoreData) return
                if (!recyclerView.canScrollVertically(1)) {
                    isUserScrolling = false
                    isLoading = true
                    currentPage++
                    loadSuperMarket()
                }
            }
        })
        loadSuperMarket()

    }

    private fun loadSuperMarket(){
        if (isOnline(requireActivity())) {
            getSuperMarketsList(currentPage)
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun updateButtonStyles(selected: View, unselected: View) {
        selected.setBackgroundResource(R.drawable.selected_button_bg)
        unselected.setBackgroundResource(R.drawable.unselected_button_bg)
        (selected as TextView).setTextColor(Color.WHITE)
        (unselected as TextView).setTextColor(Color.BLACK)
    }

    private fun getSuperMarketsList(currentPage : Int) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketDetailsSuperMarketViewModel.getSuperMarketWithPage({
                BaseApplication.dismissMe()
                handleMarketApiResponse(it)
            }, latitude, longitude,currentPage.toString())
        }
    }

    private fun handleMarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleMarketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleMarketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuperMarketModel::class.java)
            Log.d("@@@ Recipe Detailsssss", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                apiModel.data?.let {
                    showUIData(apiModel.data)
                }?:run {
                    pageReset()
                }

            } else {
                pageReset()
                handleApiError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            pageReset()
            showAlert(e.message, false)
        }

    }

    private fun showUIData(data: MutableList<Store>?) {
        try {
            data?.let {
                stores.addAll(it)
            }
            if (stores.size>0){
                // Set adapter
                adapter?.updateList(stores)
                binding.recySuperMarket.adapter = adapter
                mMap?.let { updateMap(it) }
            }
            hasMoreData=true
        } catch (e: Exception) {
            showAlert(e.message, false)
        }finally {
            isLoading = false
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    private fun pageReset(){
        if (currentPage!=1){
            currentPage--
        }
        isLoading = false
        hasMoreData = true
        isUserScrolling = true

    }

    private fun handleApiError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }



    // Add markers to map
    private fun updateMap(map: GoogleMap) {
        map.clear()
        stores.let {
            stores.forEach {
                val location= it.address?.longitude?.let { it1 -> it.address.latitude?.let { it2 ->
                    LatLng(it2, it1) } }
                val imageUrl = it.image
                val name = it.store_name
                Glide.with(requireContext())
                    .asBitmap()
                    .load(imageUrl)
                    .override(100, 100) // Resize the marker image
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            val markerOptions = location?.let { it1 ->
                                MarkerOptions()
                                    .position(it1)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resource))
                            }
                            if (markerOptions != null) {
                                mMap?.addMarker(markerOptions)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Optional: Handle placeholder or cleanup
                        }
                    })


                location?.let { it1 -> CameraUpdateFactory.newLatLngZoom(it1, 10f) }
                    ?.let { it2 -> map.moveCamera(it2) }
            }
        }

    }

    override fun onMapReady(gmap: GoogleMap) {
        mMap = gmap

    }

    // Manage MapView Lifecycle
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun itemSelectUnSelect(id: Int?, status: String?, type: String?, position: Int?) {
        if (type == "SuperMarket") {
            storeUid = position?.let { stores?.get(it)?.store_uuid.toString() }
            storeName = position?.let { stores?.get(it)?.store_name.toString() }
            if (isOnline(requireActivity())) {
                selectSuperMarketApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun selectSuperMarketApi() {
        lifecycleScope.launch {
            basketDetailsSuperMarketViewModel.selectStoreProductUrl({
                BaseApplication.dismissMe()
                handleSelectSupermarketApiResponse(it)
            }, storeName, storeUid)
        }
    }

    private fun handleSelectSupermarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuperMarketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuperMarketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                (activity as MainActivity?)?.upBasket()
                findNavController().navigateUp()
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


}