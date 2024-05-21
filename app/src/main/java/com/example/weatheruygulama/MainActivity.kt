package com.example.weatheruygulama

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatheruygulama.databinding.ActivityMainBinding
import com.example.weatheruygulama.utils.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val CHANNEL_ID = "weather_channel"
        const val CHANNEL_NAME = "Weather Channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        // binding nesnesini kullanarak view'lara erişim
        val mainLayout: ConstraintLayout = binding.main
        val switchBackground = binding.switchBackground
        val btnNotify = binding.btnNotify

        // Switch için OnCheckedChangeListener ekleme
        switchBackground.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mainLayout.setBackgroundColor(Color.GREEN)
            } else {
                mainLayout.setBackgroundColor(Color.WHITE)
            }
        }

        // Bildirim butonu için OnClickListener ekleme
        btnNotify.setOnClickListener {
            sendNotification(binding.tvTemp.text.toString())
        }

        // Hava durumu verilerini alma fonksiyonunu çağırma
        getCurrentWeather()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for weather notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(temp: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Bildirim için küçük bir ikon ekleyin
            .setContentTitle("Current Weather")
            .setContentText("Temperature: $temp")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun getCurrentWeather() {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getCurrentWeather("İzmir", "metric", getString(R.string.api_key))
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "App error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@launch
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "HTTP error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    binding.tvTemp.text = "Temp: ${response.body()!!.main.temp}"
                }
            }
        }
    }
}
