package com.example.taurusgamevault

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.imageLoader
import coil.request.ImageRequest
import com.example.taurusgamevault.Model.room.entities.GameList
import com.example.taurusgamevault.classes.SupabaseImageHelper
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        viewModel.getListNames(application)

        setupNavigation()

        setupDrawer()

        observeGameLists()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mainFragment, R.id.gameListFragment),
            drawerLayout
        )

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        findViewById<NavigationView>(R.id.nav_view)
            .setupWithNavController(navController)
    }

    private fun setupDrawer() {
        val navView = findViewById<NavigationView>(R.id.nav_view)

        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationClick(menuItem)
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun observeGameLists() {
        viewModel.lists?.observe(this) { gameLists ->
            updateDrawerGameLists(gameLists)
        }
    }

    private fun updateDrawerGameLists(gameLists: List<GameList>) {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navView.menu

        val SUB_GROUP_ID = 99

        menu.removeGroup(SUB_GROUP_ID)

        if (gameLists.isEmpty()) return

        gameLists.forEach { gameList ->
            val menuItem = menu.add(
                SUB_GROUP_ID,
                gameList.list_id.toInt(),
                Menu.NONE,
                gameList.name
            )

            menuItem.setIcon(R.drawable.starimg)

            val imageUrl = SupabaseImageHelper.getImageUrl(gameList.image)

            imageUrl?.let { url ->
                val request = ImageRequest.Builder(this)
                    .data(url)
                    .size(64)
                    .listener(
                        onSuccess = { _, result ->
                            menuItem.icon = result.drawable
                        },
                        onError = { _, error ->
                            menuItem.setIcon(R.drawable.starimg)
                            Log.e("MenuImage", "Error: ${error.throwable.message}")
                        }
                    )
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }

    private fun handleNavigationClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.mainFragment -> {
                navController.navigate(R.id.mainFragment)
            }
            R.id.gameListFragment -> {
                navController.navigate(R.id.gameListFragment)
            }
            else -> {
                val gameList = viewModel.lists?.value?.find {
                    it.list_id.toInt() == menuItem.itemId
                }

                gameList?.let {
                    val bundle = Bundle().apply {
                        putLong("listId", it.list_id)
                        putBoolean("editMode", false)
                    }
                    navController.navigate(R.id.gameListDetailFragment, bundle)
                }
            }
        }

        findViewById<DrawerLayout>(R.id.drawer_layout)
            .closeDrawer(GravityCompat.START)

        return true
    }
}