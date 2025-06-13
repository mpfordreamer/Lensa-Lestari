package com.example.lensalestari.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lensalestari.R
import com.example.lensalestari.adapter.HistoryAdapter
import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.data.repository.HistoryRepository
import com.example.lensalestari.databinding.FragmentHomeBinding
import com.example.lensalestari.factory.ViewModelFactory
import com.example.lensalestari.ui.auth.LoginActivity
import com.example.lensalestari.utils.SessionManager

/**
 * Fragment untuk menampilkan halaman utama (Home).
 * Terintegrasi dengan ViewModel untuk menampilkan riwayat analisis dan statistik.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var sessionManager: SessionManager

    // Inisialisasi ViewModel dengan Factory yang benar
    private val viewModel: HomeViewModel by viewModels {
        val apiService = ApiClient.getInstance(requireContext())
            ?: throw IllegalStateException("ApiService is null. Pastikan base URL sudah diatur.")
        ViewModelFactory(
            HistoryRepository.getInstance(apiService)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets()
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupHeaderAndProfile()
        observeViewModel()

        // Memanggil fungsi untuk mengambil semua data
        fetchHomeData()
    }

    /**
     * Menginisialisasi RecyclerView dan HistoryAdapter.
     */
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
    }

    /**
     * Mengamati perubahan data dari LiveData di ViewModel.
     * Memperbarui daftar riwayat dan kartu statistik.
     */
    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Pastikan Anda punya ProgressBar dengan id 'progress_bar' di layout
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Mengamati daftar riwayat analisis
        viewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            // Menampilkan pesan jika riwayat kosong (opsional)
            historyAdapter.submitList(historyList)
        }

        // BARU: Mengamati data statistik sampah dari API
        viewModel.trashStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                // Langsung perbarui UI dari data API
                binding.tvTotalCount.text = it.total.toString()
                binding.tvOrganicCount.text = it.organik.toString()
                binding.tvInorganicCount.text = it.anorganik.toString()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Memulai proses pengambilan SEMUA data dari API untuk halaman home.
     */
    private fun fetchHomeData() {
        val token = sessionManager.getAuthToken()
        if (token != null) {
            // Memanggil fungsi baru di ViewModel
            viewModel.fetchAllHomeData("Bearer $token")
        } else {
            logout()
        }
    }

    /**
     * Mengatur UI untuk header dan ikon profil.
     */
    private fun setupHeaderAndProfile() {
        val userName = sessionManager.getUserName()
        binding.tvHeaderName.text = "Hai, ${userName ?: "User"}!"
        binding.ivProfileIcon.setOnClickListener { anchorView ->
            showProfileMenu(anchorView)
        }
    }

    /**
     * Menampilkan PopupMenu yang terikat pada sebuah View.
     */
    private fun showProfileMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    /**
     * Melakukan proses logout.
     */
    private fun logout() {
        sessionManager.clearAuthData()
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}