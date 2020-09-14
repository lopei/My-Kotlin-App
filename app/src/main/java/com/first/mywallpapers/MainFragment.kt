package com.first.mywallpapers


import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.azoft.carousellayoutmanager.CarouselLayoutManager
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.azoft.carousellayoutmanager.CenterScrollListener
import com.first.mywallpapers.databinding.FragmentMainBinding
import java.io.*


class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ImageAdapter
    private lateinit var layoutManager: CarouselLayoutManager
    private lateinit var images: Array<String>
    private var position: Int = 0

    private val PERMISSIONS = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        initRecycler()
        initObjects()
        initInfoTexts()
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun initRecycler() {
        val assetManager: AssetManager = requireContext().assets
        images = assetManager.list("") as Array<String>
        layoutManager = CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL)
        adapter = ImageAdapter(images, context)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addOnScrollListener(CenterScrollListener())
        binding.recyclerView.adapter = adapter
        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager.centerItemPosition != position) {
                    initInfoTexts()
                    position = layoutManager.centerItemPosition
                }

            }
        })

    }

    private fun initObjects() {
        binding.arrowLeft.setOnClickListener() {
            var position =  0
            if ( layoutManager.centerItemPosition > 0) {
                position =  layoutManager.centerItemPosition - 1
            }
           binding.recyclerView.scrollToPosition(position)
        }
        binding.arrowRight.setOnClickListener() {
            var position =  0
            if ( layoutManager.centerItemPosition < layoutManager.itemCount) {
                position =  layoutManager.centerItemPosition + 1
            }
            binding.recyclerView.scrollToPosition(position)
        }

        binding.setWallpaper.setOnClickListener() {
            val myWallpaperManager: WallpaperManager =
                WallpaperManager.getInstance(context)
            try {
                val d =
                    Drawable.createFromStream(context?.assets!!.open(images[layoutManager.centerItemPosition]), null)

                myWallpaperManager.setBitmap((d as BitmapDrawable).bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        binding.star.setOnClickListener() {
            Toast.makeText(activity, getString(R.string.add_favourite), Toast.LENGTH_LONG).show()
        }
    }

    fun initInfoTexts() {
        val random = (2347..8740).random()
        binding.downloaded.text = String.format(getString(R.string.downloaded), random.toString())
        binding.viewing.text = String.format(getString(R.string.viewing), (random * 1.5).toInt().toString())
        val pixelsArray = arrayListOf("1080*1920", "2160*3840", "2560*1600", "2048*1536")
        binding.pixels.text = String.format(getString(R.string.px), pixelsArray[(0..3).random()])
        val array = arrayListOf("2.3", "3.5", "5.9", "6.7", "8.7")
        binding.size.text = String.format(getString(R.string.kb), array[(0..4).random()])
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (hasPermissions(context, PERMISSIONS)) {
            binding.download.setOnClickListener() {
                copyAssets(images[layoutManager.centerItemPosition])
            }
        } else {
            requestPermissions(PERMISSIONS.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun copyAssets(filename: String) {
        val assetManager = requireContext().assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            `in` = assetManager.open(filename)
            val root: File = Environment.getExternalStorageDirectory()
            val dir = File(root.absolutePath.toString() + "/download")
            dir.mkdirs()
            val file = File(dir, filename)
            out = FileOutputStream(file)
            copyFile(`in`, out)
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: $filename", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private fun hasPermissions(context: Context?, permissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            return permissions.all { permission ->
                ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE && hasPermissions(context, PERMISSIONS)) {
            binding.download.setOnClickListener() {
                copyAssets(images[layoutManager.centerItemPosition])
            }
        }
    }
}
