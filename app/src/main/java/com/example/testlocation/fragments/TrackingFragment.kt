package com.example.testlocation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.testlocation.R
import com.example.testlocation.databinding.FragmentTrackingBinding
import com.example.testlocation.helper.*
import com.example.testlocation.respositories.TrackingRepository
import com.example.testlocation.viewmodel.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) , EasyPermissions.PermissionCallbacks{

    private var _binding: FragmentTrackingBinding? = null
    private val trackingViewModel: TrackingViewModel by viewModels()
    private val binding get() = _binding!!
    private lateinit var mapLifecycleObserver: MapLifecycleObserver
    private var map: GoogleMap? = null
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        mapLifecycleObserver = MapLifecycleObserver(mapView, lifecycle)

        subscribeToObservers()
        binding.apply {
            lifecycleOwner = this@TrackingFragment
            viewModel = trackingViewModel

            btnToggleRun.setOnClickListener {
                trackingViewModel.sendCommandToService(requireContext())

            }



            btnFinishRun.setOnClickListener { finishRun() }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TrackingUtility.requestPermissions(this)
        mapView?.let { mapView ->
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync {
                map = it
                addAllPolylines()

            }
        }

    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            TrackingUtility.requestPermissions(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onStart() {
        super.onStart()
    }


    private fun subscribeToObservers() {




        trackingViewModel.isTracking.observe(viewLifecycleOwner, Observer {
            binding.btnToggleRun.text = if(it) "PAUSE" else "START"
        })


        trackingViewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            addLatestPolyline()
            moveCameraToUser()
        })

        trackingViewModel.currentTimeInMillis.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(it, true)
            //binding.tvTimer.text = formattedTime
        })
    }



    private fun moveCameraToUser() {
        if (TrackingRepository.pathPoints.value!!.isNotEmpty() && TrackingRepository.pathPoints.value!!.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(TrackingRepository.pathPoints.value!!.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun addAllPolylines() {

        trackingViewModel.getDBLocations().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)

                CameraUpdateFactory.newLatLngZoom(LatLng(it[0].latitude.toDouble(),it[0].longitude.toDouble()), MAP_ZOOM)
                it?.forEach {
                    polylineOptions.add(LatLng(it.latitude.toDouble(),it.longitude.toDouble()))
                }
                map?.addPolyline(polylineOptions)
            }
        }

    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in TrackingRepository.pathPoints.value!!) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        val latLngBounds = try {
            bounds.build()
        } catch (e: IllegalStateException) {
            Timber.e(e, "Cannot find any path points, associated with this run")
            return
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                mapView!!.width,
                mapView!!.height,
                (mapView!!.height * 0.05f).toInt()
            )
        )
    }

    private fun finishRun() {
        zoomToSeeWholeTrack()
        trackingViewModel.processRun(requireContext())

    }

    private fun addLatestPolyline() {
        if (TrackingRepository.pathPoints.value!!.isNotEmpty() && TrackingRepository.pathPoints.value!!.last().size > 1) {
            val preLastLatLng = TrackingRepository.pathPoints.value!!.last()[TrackingRepository.pathPoints.value!!.last().size - 2]
            val lastLatLng = TrackingRepository.pathPoints.value!!.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }






    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
