package com.mobiiworld.ui.activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobiiworld.R
import com.mobiiworld.ui.fragments.ListFragment


class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Begin the transaction
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Replace the contents of the container with the new fragment
        val fragment = ListFragment.newInstance()
        fragmentTransaction.replace(R.id.fragment_container, fragment)

        // Complete the changes added above
        fragmentTransaction.commit()
    }
}

