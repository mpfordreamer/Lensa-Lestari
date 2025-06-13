package com.example.lensalestari.ui.reward

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lensalestari.R
import com.example.lensalestari.adapter.ContentAdapter
import com.example.lensalestari.adapter.RewardHistoryAdapter
import com.example.lensalestari.data.api.ApiClient
import com.example.lensalestari.data.model.ContentItem
// PERUBAHAN: Ganti model dummy dengan model dari API
import com.example.lensalestari.data.model.PointHistoryItem
import com.example.lensalestari.data.repository.PointHistoryRepository
import com.example.lensalestari.databinding.FragmentRewardBinding
import com.example.lensalestari.factory.ViewModelFactory
import com.example.lensalestari.ui.auth.LoginActivity
import com.example.lensalestari.ui.detail.ContentDetailActivity
import com.example.lensalestari.utils.SessionManager

class RewardFragment : Fragment() {

    private var _binding: FragmentRewardBinding? = null
    private val binding get() = _binding!!

    // BARU: Deklarasi ViewModel, Adapter, dan SessionManager
    private lateinit var rewardHistoryAdapter: RewardHistoryAdapter
    private lateinit var sessionManager: SessionManager

    // BARU: Inisialisasi ViewModel menggunakan pola dari HomeFragment
    private val viewModel: PointHistoryViewModel by viewModels {
        val apiService = ApiClient.getInstance(requireContext())
            ?: throw IllegalStateException("ApiService is null. Pastikan base URL sudah diatur.")
        ViewModelFactory(
            PointHistoryRepository.getInstance(apiService)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRewardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupUserData()
        setupAllRecyclerViews()

        // BARU: Amati perubahan dari ViewModel dan mulai ambil data
        observeViewModel()
        fetchPointHistoryData()
    }

    /**
     * Mengamati perubahan LiveData dari PointHistoryViewModel.
     */
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Pastikan Anda punya ProgressBar di layout dengan id 'progressBar'
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.pointHistory.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                // Opsional: tampilkan pesan jika tidak ada riwayat
                binding.tvEmptyHistory.visibility = View.VISIBLE
                binding.rvRewardHistory.visibility = View.GONE
            } else {
                binding.tvEmptyHistory.visibility = View.GONE
                binding.rvRewardHistory.visibility = View.VISIBLE
                // Kirim data ke adapter
                rewardHistoryAdapter.submitList(historyList)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Memulai proses pengambilan data riwayat poin dari API.
     */
    private fun fetchPointHistoryData() {
        val token = sessionManager.getAuthToken()
        if (token != null) {
            viewModel.fetchPointHistory(token)
        } else {
            // Jika token tidak ada, logout pengguna
            logout()
        }
    }

    /**
     * Titik utama untuk menginisialisasi semua RecyclerView yang ada di fragment ini.
     */
    private fun setupAllRecyclerViews() {
        setupRewardHistoryList() // Ini akan dihubungkan ke ViewModel
        setupNewsList()          // Ini masih menggunakan data dummy
        setupEducationList()     // Ini masih menggunakan data dummy
        setupEventList()         // Ini masih menggunakan data dummy
    }


    /**
     * Mengatur data yang spesifik untuk pengguna, seperti total poin dan badge.
     */
    private fun setupUserData() {
        val userPoints = sessionManager.getUserPoin()
        binding.tvUserPoints.text = userPoints.toString()

        val (badgeName, badgeIconResId) = when {
            userPoints >= 2000 -> "Gold Badge" to R.drawable.badge_circle_gold
            userPoints >= 1000 -> "Silver Badge" to R.drawable.badge_circle_silver
            userPoints >= 500 -> "Bronze Badge" to R.drawable.badge_circle_bronze
            else -> "No Badge" to R.drawable.badge_circle_none
        }

        binding.tvBadgeName.text = badgeName
        binding.ivBadgeIcon.setImageResource(badgeIconResId)
    }

    /**
     * PERUBAHAN: Mengatur RecyclerView untuk menampilkan riwayat poin dari ViewModel.
     * Data dummy dihapus.
     */
    private fun setupRewardHistoryList() {
        // Inisialisasi adapter dengan list kosong. Data akan diisi oleh LiveData.
        rewardHistoryAdapter = RewardHistoryAdapter()
        binding.rvRewardHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rewardHistoryAdapter
        }
        // PENTING: Pastikan RewardHistoryAdapter Anda bisa menerima List<PointHistoryItem>
        // dan bukan lagi RewardHistoryItem (model dummy lama).
    }

    private fun setupNewsList() {
        // Data News
        val newsList = listOf(
            ContentItem(
                title = "Planawood, Startup yang Ubah Limbah Plastik jadi Produk Pengganti Kayu",
                description = "Di tangan startup Planawood, limbah plastik diolah menjadi bahan bangunan dan interior alternatif yang ramah lingkungan.",
                type = "News",
                category = "News",
                date = "Minggu, 04 Agustus 2024",
                imageName = "planawood",
                fullContent = """
                Di tangan startup Planawood, limbah plastik mampu diolah menjadi barang bernilai tinggi loh. Planawood mengubah limbah plastik menjadi bahan bangunan dan interior seperti decking, beam atau balok, brick hingga ubin.
                
                Tidak hanya limbah plastik, Joshua Christopher selaku founder dari Planawood mengatakan, mereka menggunakan bahan dasar lain juga berupa limbah gabah padi yang dikumpulkan langsung melalui pengepul.
                
                “Kekhawatiran kami berangkat dari persoalan sampah plastik di Indonesia yang menumpuk dan memang sulit terurai secara alami,” ujar Joshua kepada Mediahijau.com, pekan ini.
                
                Secara singkat, limbah sampah plastik dan gabah padi yang telah dikumpulkan dari pengepul kemudian diolah untuk menjadi material baru berupa bahan bangunan dan interior alternatif yang ramah lingkungan sebagai pengganti kayu atau komposit kayu plastik (wood plastic composite/WPC).
                
                Berbagai produk daur ulang limbah plastik racikan Planawood ini ditawarkan melalui berbagai platform belanja online dengan harga terjangkau. Contoh, cukup dengan merogoh kocek Rp 150.000, kamu sudah bisa memiliki dudukan handphone berbahan dasar limbah plastik yang anti air, anti rayap, dan tahan lama.
                
                Dalam menarik minat konsumen untuk beralih kepada produk ramah lingkungan, Planawood secara aktif berpromosi lewat berbagai kegiatan sekaligus meningkatkan kesadaran dan kepedulian terhadap lingkungan serta pola hidup yang berkelanjutan.
                
                “Kami aktif mengikuti event seperti talkshow untuk meningkatkan awareness dan pemasaran seperti DBS Asian Insight Conference 2024, juga edukasi secara online tentang lingkungan melalui Instagram kami @plasticfornature,” ujar Joshua.
                
                Selain ramah lingkungan, Planawood juga menawarkan kualitas dan harga yang mampu bersaing dengan produk kayu konvensional di pasaran. Bahan dasar kayu cenderung mudah lembab dan rawan diserang rayap, sedangkan produk milik Planawood yang berbahan dasar plastik dan sekam padi, dirancang untuk tahan lama, kuat, dan bebas dari rayap.
                
                Di masa depan, Joshua menyatakan Planawood akan terus fokus pada peningkatan kesadaran dan kepedulian terhadap lingkungan, sambil tetap meningkatkan kualitas produk untuk bersaing dengan produk konvensional.
                
                Saat ini, beberapa perusahaan besar seperti Summarecon, Hyundai, dan Astra telah menggunakan produk ramah lingkungan dari Planawood. Selain itu, Joshua juga berencana untuk merambah pemasaran kepada pengembang perumahan.
                """.trimIndent()
            ),
            ContentItem(
                title = "Larangan Plastik Sekali Pakai di Bali Mulai Berbuah Hasil",
                description = "Kebijakan larangan plastik sekali pakai di Bali sejak 2019 berhasil menurunkan volume sampah plastik hingga 52% di pesisir.",
                type = "News",
                category = "News",
                date = "Senin, 05 Agustus 2024", // Ganti jika ingin tanggal lain
                imageName = "pemprov_bali",
                fullContent = """
                Denpasar – Kebijakan larangan penggunaan plastik sekali pakai yang diterapkan sejak 2019 di Bali mulai menunjukkan hasil positif. Pemerintah Provinsi Bali menyebutkan bahwa volume sampah plastik di pesisir pantai berkurang hingga 52% dalam dua tahun terakhir. Ini merupakan hasil dari pengawasan ketat serta dukungan masyarakat dan pelaku usaha lokal.
                
                Pasar tradisional, toko oleh-oleh, hingga hotel kini sudah tidak lagi menyediakan kantong plastik atau sedotan berbahan plastik. Sebagai gantinya, penggunaan tas kain, sedotan bambu, dan kemasan ramah lingkungan mulai menjadi kebiasaan. Wisatawan juga mulai terbiasa membawa tumbler dan peralatan makan sendiri.
                
                Gubernur Bali I Wayan Koster menyatakan bahwa regulasi ini merupakan bagian dari upaya menjaga kelestarian alam dan menunjang sektor pariwisata. Ia berharap provinsi lain dapat mengikuti langkah Bali dalam menciptakan pariwisata berkelanjutan yang tidak merusak lingkungan. Bali saat ini juga sedang merancang aturan lanjutan untuk pengelolaan sampah elektronik dan bahan kimia.
                """.trimIndent()
            ),
            ContentItem(
                title = "Garuda Indonesia Gelar Aksi Bersih Sungai Ciliwung",
                description = "Memperingati Hari Peduli Sampah Nasional 2025, Garuda Indonesia dan Komunitas Alkesa mengadakan aksi bersih Sungai Ciliwung dan pembangunan fasilitas sanitasi.",
                type = "News",
                category = "News",
                date = "Jumat, 21 Februari 2025",
                imageName = "tjsl_ciliwung", // simpan gambar dengan nama tjsl_ciliwung di drawable
                fullContent = """
                Bogor, 21 Februari 2025 - Dalam rangka memperingati Hari Peduli Sampah Nasional 2025, pada Jumat (21/2), Garuda Indonesia melaksanakan program aksi bersih Sungai Ciliwung di area Tempat Wisata Edukasi Panorama Sungai Ciliwung, Saung Alkesa, Bogor, Jawa Barat.
                
                Direktur Utama Garuda Indonesia, Wamildan Tsani Panjaitan menyatakan bahwa pelaksanaan aksi bersih Sungai Ciliwung ini menjadi wujud komitmen berkelanjutan Garuda Indonesia dalam mengimplementasikan misi Perusahaan sebagai “green airline”, khususnya melalui aksi nyata mendukung kelestarian lingkungan.
                
                Pada kesempatan yang sama, Direktur Utama Garuda Indonesia Wamildan Tsani Panjaitan, turut meresmikan pembangunan fasilitas sanitasi umum, yang meliputi penyediaan sarana MCK dan tempat wudhu di Tempat Wisata Edukasi Panorama Sungai Ciliwung, Saung Alkesa, Bogor, Jawa Barat. Adapun dukungan tersebut terwujud melalui kolaborasi bersama komunitas Alkesa Bumi Belajar.
                
                “Ketersediaan fasilitas sanitasi umum serta aksi bersih-bersih Sungai Ciliwung ini, turut merepresentasikan upaya pembangunan berkelanjutan Garuda Indonesia yang merupakan salah satu pilar inisiatif sustainability sebagai program strategis yang dijalankan Perusahaan. Adapun program ini juga turut menjadi bagian dari misi Program Tanggung Jawab Sosial Lingkungan yang dicanangkan Garuda Indonesia di tahun 2025.
                
                Sejalan dengan tema peringatan Hari Peduli Sampah Nasional 2025 bertajuk “Kolaborasi Untuk Indonesia Bersih”, diharapkan kolaborasi antara Garuda Indonesia dan Komunitas Alkesa Bumi Belajar ini dapat memberikan kebermanfaatan yang luas bagi masyarakat, serta mampu melahirkan kesadaran dan aksi kolektif yang berdampak positif bagi lingkungan dan bumi di masa depan,” jelas Wamildan.
                """.trimIndent()
            )
        )
        setupHorizontalContentList(binding.rvNews, newsList)
    }
    private fun setupEducationList() {
        // Data Edukasi
        val educationList = listOf(
            ContentItem(
                title = "Bahaya Mikroplastik bagi Kesehatan",
                description = "Kenali mikroplastik, partikel plastik kecil yang tersebar di sungai, tanah, dan bahkan makanan. Apa bahayanya bagi kesehatan dan lingkungan kita?",
                type = "News",
                category = "Edukasi",
                date = "Rabu, 12 Juni 2025",
                imageName = "mikroplastik", // simpan ilustrasi mikroplastik di drawable
                fullContent = """
                Mikroplastik adalah partikel plastik berukuran kurang dari 5 milimeter yang kini ditemukan di mana-mana: air sungai, tanah, udara, bahkan makanan dan minuman yang kita konsumsi sehari-hari. Partikel kecil ini berasal dari penguraian sampah plastik yang tidak terkelola, atau dari produk sehari-hari seperti scrub wajah dan botol plastik sekali pakai. 
                
                Dampak mikroplastik sangat nyata. Studi menemukan bahwa mikroplastik mengganggu ekosistem sungai, masuk ke tubuh ikan dan akhirnya ke rantai makanan manusia. Risiko bagi kesehatan pun tidak main-main: mikroplastik dapat menyebabkan gangguan hormon, peradangan, hingga meningkatkan risiko kanker dan penyakit kronis jika terakumulasi dalam tubuh.
                
                Bali telah mengambil langkah dengan melarang botol air minum plastik kecil, namun perubahan nyata butuh dukungan masyarakat. Aplikasi Lensa Lestari hadir untuk membantu mendeteksi dan melaporkan sampah plastik di sungai, sekaligus mengedukasi pengguna tentang bahaya mikroplastik. Mulailah dari langkah kecil: gunakan tumbler, hindari plastik sekali pakai, dan laporkan sampah lewat aplikasi. Dengan aksi bersama, Bali yang lebih bersih dan bebas mikroplastik bukan lagi mimpi.
                """.trimIndent()
            )
        )
        setupHorizontalContentList(binding.rvEducation, educationList)
    }

    private fun setupEventList() {
        // Data Event
        val eventList = listOf(
            ContentItem(
                title = "Gerakan Bali Bersih Sampah",
                description = "Gerakan kolaboratif seluruh masyarakat dan pelaku usaha Bali untuk mewujudkan pulau bebas sampah plastik sekali pakai.",
                type = "News",
                category = "Event",
                date = "Jumat, 11 April 2025",
                imageName = "pemprov_bali", // Simpan gambar terkait di drawable, misal bali_bersih_sampah.png
                fullContent = """
                Provinsi Bali menggelar "Gerakan Bali Bersih Sampah" sebagai ajakan nyata kepada seluruh masyarakat, pelaku usaha, dan aparat desa untuk berkomitmen menciptakan Bali yang asri dan bebas dari sampah plastik sekali pakai. Gerakan ini lahir dari Peraturan Gubernur No. 47 Tahun 2019 dan diperkuat dengan Surat Edaran Gubernur No. 9 Tahun 2025 sebagai dasar hukum pengelolaan sampah berbasis sumber. Desa, kelurahan, dan desa adat menjadi garda terdepan, wajib mensosialisasikan pemilahan sampah di tingkat rumah tangga serta memastikan aturan adat terkait sampah benar-benar dijalankan. Pelaku usaha di sektor pariwisata, pasar, tempat ibadah, hingga lembaga pendidikan juga dituntut untuk mengelola sampah secara mandiri dan membatasi penggunaan plastik sekali pakai sebagai bukti kepedulian terhadap lingkungan.
                
                Seluruh warga Bali dan pengunjung diimbau untuk mulai memilah sampah dan bertanggung jawab atas limbah yang dihasilkan, karena aksi sederhana dari tiap individu akan membawa dampak besar. Pemerintah Provinsi Bali menegaskan komitmen ini melalui konsekuensi yang tegas: wilayah yang gagal mengelola sampah berpotensi kehilangan bantuan keuangan, sementara pelaku usaha yang lalai dapat dikenai sanksi pencabutan izin usaha dan publikasi negatif.
                
                Gerakan ini mendapat dukungan penuh dari Kementerian Lingkungan Hidup, dan sebagai bentuk apresiasi, Pemprov Bali akan rutin memberikan penghargaan kepada desa, kelurahan, desa adat, maupun pelaku usaha yang paling berinovasi dan disiplin dalam pengelolaan sampah. Penghargaan tersebut meliputi piagam dari Gubernur, publikasi, hingga insentif lainnya. Dengan semangat gotong royong dan persaingan dalam kebaikan, diharapkan Bali bisa menjadi contoh provinsi bersih dan lestari bagi Indonesia.
                """.trimIndent()
            ),
            ContentItem(
                title = "Hari Peduli Sampah Nasional 2025 Bandung",
                description = "Bandung rayakan HPSN 2025 dengan aksi bersih-bersih dan berbagai program pengelolaan sampah kolaboratif lintas komunitas.",
                type = "News",
                category = "Event",
                date = "Jumat, 21 Februari 2025",
                imageName = "hpsn_bandung", // simpan gambar terkait di drawable, misal hpsn_bandung.png
                fullContent = """
                Pemerintah Kota Bandung mengajak seluruh masyarakat untuk bersatu memperingati Hari Peduli Sampah Nasional (HPSN) 2025. Momentum ini menjadi pengingat akan pentingnya pengelolaan sampah demi lingkungan yang sehat dan kota yang nyaman, sejalan dengan peringatan tragedi TPA Leuwigajah tahun 2005. Serangkaian kegiatan pun digelar, mulai dari peresmian mesin pemusnahan sampah di Kecamatan Bandung Kulon, pengiriman perdana RDF ke PT Indocement, hingga penyerahan bantuan sarana pengelolaan sampah untuk komunitas oleh Bank BJB.
                
                Pada puncak HPSN tanggal 21 Februari 2025, masyarakat diajak untuk berpartisipasi dalam aksi bersih-bersih pasar serentak, memperlihatkan komitmen nyata terhadap pengelolaan sampah dari tingkat rumah tangga hingga kota. Kepala Dinas Lingkungan Hidup Kota Bandung menekankan pentingnya kolaborasi lintas elemen masyarakat untuk mewujudkan pengelolaan sampah yang lebih baik.
                
                Sebagai bentuk apresiasi, Pemda Kota Bandung akan memberikan penghargaan kepada individu, komunitas, RW, atau kelurahan yang paling berinisiatif dan berpartisipasi aktif dalam pengelolaan sampah berkelanjutan. Inovasi, partisipasi, dan dampak lingkungan positif menjadi kriteria utama penilaian, dan penerima apresiasi akan dipublikasikan sebagai inspirasi bagi warga lain. Jadikan HPSN 2025 sebagai momentum untuk memperkuat komitmen bersama menuju Bandung yang bersih, sehat, dan nyaman untuk semua.
                """.trimIndent()
            )
        )
        setupHorizontalContentList(binding.rvEvents, eventList)
    }

    private fun setupHorizontalContentList(recyclerView: RecyclerView, contentList: List<ContentItem>) {
        val contentAdapter = ContentAdapter(contentList)
        recyclerView.adapter = contentAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        contentAdapter.setOnItemClickCallback { selectedContent ->
            val intent = Intent(requireContext(), ContentDetailActivity::class.java)
            intent.putExtra(ContentDetailActivity.EXTRA_CONTENT, selectedContent)
            startActivity(intent)
        }
    }

    // BARU: Fungsi logout jika token tidak valid
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