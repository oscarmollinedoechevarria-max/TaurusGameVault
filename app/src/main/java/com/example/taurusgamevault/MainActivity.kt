package com.example.taurusgamevault

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
import com.example.taurusgamevault.databinding.ActivityMainBinding
import com.example.taurusgamevault.mainscreen.MainFragment
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check if dark mode is on
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // initializations
        viewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        viewModel.getListNames(application)

        viewModel.getPlataforms(application)

        setupNavigation()

        setupDrawer()

        observeGameLists()
    }

    // setup navigation and visuals
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout = binding.drawerLayout

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.mainFragment, R.id.gameListFragment, R.id.listTagsFragment),
            drawerLayout
        )

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        val themeOverlay = if (isDarkMode) {
            R.style.ThemeOverlay_TaurusGameVault_Toolbar_Night
        } else {
            R.style.ThemeOverlay_TaurusGameVault_Toolbar
        }
        toolbar.context

        val iconColor = ContextCompat.getColor(this, R.color.light_on_primary) // #FFFFFF

        toolbar.navigationIcon?.let {
            DrawableCompat.setTint(it, iconColor)
        }
        toolbar.setTitleTextColor(iconColor)
        toolbar.overflowIcon?.let {
            DrawableCompat.setTint(it, iconColor)
        }

        val navView = binding.navView
        navView.setupWithNavController(navController)

        navView.itemIconTintList = null

        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnSearchToolbar)?.setOnClickListener {
            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            if (currentFragment is MainFragment) {
                currentFragment.toggleSearchBarVisibility()
            }
        }
    }

    private fun setupDrawer() {
        val navView = binding.navView

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

    // drawer from db
    private fun updateDrawerGameLists(gameLists: List<GameList>) {
        val navView = binding.navView
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
            R.id.listTagsFragment -> {
                navController.navigate(R.id.listTagsFragment)
            }
            R.id.appConfigurationFragment ->{
                navController.navigate(R.id.appConfigurationFragment)
            }
            else -> {
                val gameList = viewModel.lists?.value?.find {
                    it.list_id.toInt() == menuItem.itemId
                }

                gameList?.let {
                    val bundle = Bundle().apply {
                        putLong("listId", it.list_id)
                        putString("listName", it.name)
                        putBoolean("editMode", false)
                    }
                    navController.navigate(R.id.gameListDetailFragment, bundle)
                }
            }
        }

        binding.drawerLayout
            .closeDrawer(GravityCompat.START)

        return true
    }
}
