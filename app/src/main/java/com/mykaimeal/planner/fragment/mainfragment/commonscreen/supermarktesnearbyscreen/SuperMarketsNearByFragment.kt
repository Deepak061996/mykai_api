package com.mykaimeal.planner.fragment.mainfragment.commonscreen.supermarktesnearbyscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterSuperMarket
import com.mykaimeal.planner.basedata.BaseApplication
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

    private var stores: MutableList<Store>?=null

    private val storeLocations = mutableListOf<LatLng>() // List to store locations

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

        if (BaseApplication.isOnline(requireActivity())) {
            getSuperMarketsList()
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

    private fun getSuperMarketsList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketDetailsSuperMarketViewModel.getSuperMarket({
                BaseApplication.dismissMe()
                handleMarketApiResponse(it)
            }, latitude, longitude)
        }
    }

    private fun handleMarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleMarketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("SetTextI18n")
    private fun handleMarketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuperMarketModel::class.java)
            Log.d("@@@ Recipe Detailsssss", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                showUIData(apiModel.data)
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

    private fun showUIData(data: MutableList<Store>?) {
        try {
            if (!data.isNullOrEmpty()) {
                stores=data
                // Set adapter
                adapter = AdapterSuperMarket(stores, requireActivity(), this)
                binding.recySuperMarket.adapter = adapter

                stores?.forEach { store ->
                        val lat = store.address?.lat
                        val lng = store.address?.lon
                        val storeName = store.store_name ?: "Store"

                        if (lat != null && lng != null) {
                            val location = LatLng(lat, lng)
                            mMap?.addMarker(MarkerOptions().position(location).title(storeName)) }
                    }


                /*  // Extract LatLng and store in list
                  storeLocations.clear()
                  for (store in data) {
                      val address = store.address
                      val latitude = address?.lat ?: 0.0  // Default to 0.0 if null
                      val longitude = address?.lon ?: 0.0 // Default to 0.0 if null
                      storeLocations.add(LatLng(latitude, longitude))
                  }

                  // Update Google Map
                  mMap?.let { updateMap(it) }*/
            }
        } catch (e: Exception) {
            showAlert(e.message ?: "An error occurred", false)
        }
    }

    // Add markers to map
    private fun updateMap(map: GoogleMap) {
        map.clear()
        for (location in storeLocations) {
            map.addMarker(MarkerOptions().position(location).title("Store Location"))
        }

        // Move camera to first store location
        if (storeLocations.isNotEmpty()) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocations[0], 12f))
        }
    }

    override fun onMapReady(gmap: GoogleMap) {
        mMap = gmap
        // Add markers for all store locations
        if (storeLocations.isNotEmpty()) {
            updateMap(mMap!!)
        }
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

            if (BaseApplication.isOnline(requireActivity())) {
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