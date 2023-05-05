package org.clarkecollective.raderie.ui.main

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.room.Room
import com.google.firebase.auth.FirebaseUser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.daos.ValueDao
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.toast
import org.clarkecollective.raderie.ui.results.ResultsActivity
import org.clarkecollective.raderie.ui.share.ShareActivity

class MainActivity : AppCompatActivity() {
  private val mainActivityViewModel: MainActivityViewModel by viewModels()
  private lateinit var valueDao: ValueDao
  private lateinit var firebaseAPI: FirebaseAPI

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.addLogAdapter(AndroidLogAdapter())
    firebaseAPI = FirebaseAPI(applicationContext)

    valueDao = Room.databaseBuilder(applicationContext, MyValuesDatabase::class.java, "test-db")
      .build().valueDao()

    Logger.d("Files dir: %s",  filesDir)

    firebaseAPI.logInAndReturnUser().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(object: SingleObserver<FirebaseUser> {
        override fun onSuccess(t: FirebaseUser) {
          Logger.d("Login successful")
          Logger.d("User ID: ${t.uid}")
          mainActivityViewModel.startGame()
        }

        override fun onSubscribe(d: Disposable) {
          Logger.d("Subscribed")
        }

        override fun onError(e: Throwable) {
          Logger.e("Login Unsuccessful: ${e.message}")
        }
      }
      )
    setupBinding()
    setupObservers()
    mainActivityViewModel.dialog.observe(this) {
      createAlertDialog(it.first, it.second).show()
    }
  }

  private fun setupObservers() {
    mainActivityViewModel.menuClicked.observe(this) {
      when (it) {
        MAINMENU.SHARE -> startSharingActivity()
        MAINMENU.RESULT -> startResultsActivity()
        else -> {}
      }
    }
    mainActivityViewModel.toDefine.observe(this) {
      Logger.d("To Define: $it")
      applicationContext.toast(definitionFinder(it))
    }
  }

  private fun setupBinding() {
    val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.viewModel = mainActivityViewModel
    binding.lifecycleOwner = this
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.resultsMenuItem -> {
        startResultsActivity()
        true
      }
      R.id.sharingButton -> {
        startSharingActivity()
        true
      }
        else -> super.onOptionsItemSelected(item)
    }
  }

  private fun startSharingActivity(){
    val intent = Intent(this, ShareActivity::class.java)
    startActivity(intent)
  }

  private fun startResultsActivity() {
    val arrayDeck = mainActivityViewModel.deck.value?.let { ArrayList<HumanValue>(it) }
    val intent = arrayDeck?.let { ResultsActivity.newIntent(this, it) }
    startActivity(intent)
  }

  private fun createAlertDialog(hv1: HumanValue?, hv2: HumanValue?): AlertDialog {
    val dialog = AlertDialog.Builder(this@MainActivity).create()
    dialog.setTitle("These Words Are Synonyms...")
    dialog.setMessage("Which Is The Better Word?")
    dialog.setButton(AlertDialog.BUTTON_POSITIVE, hv1!!.name) { _, _ ->
      val success = mainActivityViewModel.removeFromDeck(hv2!!)
      if (!success) {
        Toast.makeText(applicationContext, "Item Not Found", Toast.LENGTH_SHORT).show()
      }
      else {
        Toast.makeText(applicationContext, "Deleted: " + hv2.name, Toast.LENGTH_SHORT).show()
      }
      mainActivityViewModel.pullTwo()
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, hv2!!.name) { _, _ ->
      val success = mainActivityViewModel.removeFromDeck(hv1)
      if (!success) {
        Toast.makeText(applicationContext, "Item Not Found", Toast.LENGTH_SHORT).show()
      }
      else {
        Toast.makeText(applicationContext, "Deleted: " + hv1.name, Toast.LENGTH_SHORT).show()
      }
      mainActivityViewModel.pullTwo()
    }
    dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel") { d, _ ->
      d.cancel()
    }
    return dialog
  }

  private fun definitionFinder(id: Int): String {
    when (id) {
      -2 -> { return getString(R.string.synonym_explanation) }
      -1 -> {return getString(R.string.tie_explanation) }
      0 -> { return getString(R.string.definition_0_acceptance) }
      1 -> { return getString(R.string.definition_1_accomplishment) }
      2 -> { return getString(R.string.definition_2_accountability) }
      3 -> { return getString(R.string.definition_3_accuracy) }
      4 -> { return getString(R.string.definition_4_achievement) }
      5 -> { return getString(R.string.definition_5_adaptability) }
      6 -> { return getString(R.string.definition_6_alertness) }
      7 -> { return getString(R.string.definition_7_altruism) }
      8 -> { return getString(R.string.definition_8_ambition) }
      9 -> { return getString(R.string.definition_9_amusement) }
      10 -> { return getString(R.string.definition_10_assertiveness) }
      11 -> { return getString(R.string.definition_11_attentiveness) }
      12 -> { return getString(R.string.definition_12_awareness) }
      13 -> { return getString(R.string.definition_13_balance) }
      14 -> { return getString(R.string.definition_14_beauty) }
      15 -> { return getString(R.string.definition_15_boldness) }
      16 -> { return getString(R.string.definition_16_bravery) }
      17 -> { return getString(R.string.definition_17_brilliance) }
      18 -> { return getString(R.string.definition_18_calm) }
      19 -> { return getString(R.string.definition_19_candor) }
      20 -> { return getString(R.string.definition_20_capability) }
      21 -> { return getString(R.string.definition_21_carefulness) }
      22 -> { return getString(R.string.definition_22_certainty) }
      23 -> { return getString(R.string.definition_23_challenge) }
      24 -> { return getString(R.string.definition_24_charisma) }
      25 -> { return getString(R.string.definition_25_charity) }
      26 -> { return getString(R.string.definition_26_cheerfulness) }
      27 -> { return getString(R.string.definition_27_clarity) }
      28 -> { return getString(R.string.definition_28_cleanliness) }
      29 -> { return getString(R.string.definition_29_clear_headedness) }
      30 -> { return getString(R.string.definition_30_cleverness) }
      31 -> { return getString(R.string.definition_31_comfort) }
      32 -> { return getString(R.string.definition_32_commitment) }
      33 -> { return getString(R.string.definition_33_communication) }
      34 -> { return getString(R.string.definition_34_community) }
      35 -> { return getString(R.string.definition_35_compassion) }
      36 -> { return getString(R.string.definition_36_competence) }
      37 -> { return getString(R.string.definition_37_composure) }
      38 -> { return getString(R.string.definition_38_concentration) }
      39 -> { return getString(R.string.definition_39_confidence) }
      40 -> { return getString(R.string.definition_40_conformity) }
      41 -> { return getString(R.string.definition_41_consciousness) }
      42 -> { return getString(R.string.definition_42_connection) }
      43 -> { return getString(R.string.definition_43_consciousness) }
      44 -> { return getString(R.string.definition_44_consideration) }
      45 -> { return getString(R.string.definition_45_consistency) }
      46 -> { return getString(R.string.definition_46_contentment) }
      47 -> { return getString(R.string.definition_47_contribution) }
      48 -> { return getString(R.string.definition_48_control) }
      49 -> { return getString(R.string.definition_49_conviction) }
      50 -> { return getString(R.string.definition_50_cooperation) }
      51 -> { return getString(R.string.definition_51_courage) }
      52 -> { return getString(R.string.definition_52_courtesy) }
      53 -> { return getString(R.string.definition_53_creativity) }
      54 -> { return getString(R.string.definition_54_credibility) }
      55 -> { return getString(R.string.definition_55_curiosity) }
      56 -> { return getString(R.string.definition_56_decisiveness) }
      57 -> { return getString(R.string.definition_57_decorum) }
      58 -> { return getString(R.string.definition_58_dedication) }
      59 -> { return getString(R.string.definition_59_dependability) }
      60 -> { return getString(R.string.definition_60_determination) }
      61 -> { return getString(R.string.definition_61_development) }
      62 -> { return getString(R.string.definition_62_devotion) }
      63 -> { return getString(R.string.definition_63_dignity) }
      64 -> { return getString(R.string.definition_64_discipline) }
      65 -> { return getString(R.string.definition_65_discovery) }
      66 -> { return getString(R.string.definition_66_dreams) }
      67 -> { return getString(R.string.definition_67_drive) }
      68 -> { return getString(R.string.definition_68_duty) }
      69 -> { return getString(R.string.definition_69_earnestness) }
      70 -> { return getString(R.string.definition_70_effectiveness) }
      71 -> { return getString(R.string.definition_71_efficiency) }
      72 -> { return getString(R.string.definition_72_elegance) }
      73 -> { return getString(R.string.definition_73_empathy) }
      74 -> { return getString(R.string.definition_74_endurance) }
      75 -> { return getString(R.string.definition_75_energy) }
      76 -> { return getString(R.string.definition_76_enjoyment) }
      77 -> { return getString(R.string.definition_77_enthusiasm) }
      78 -> { return getString(R.string.definition_78_equity) }
      79 -> { return getString(R.string.definition_79_ethics) }
      80 -> { return getString(R.string.definition_80_excellence) }
      81 -> { return getString(R.string.definition_81_excitement) }
      82 -> { return getString(R.string.definition_82_experience) }
      83 -> { return getString(R.string.definition_83_exercise) }
      84 -> { return getString(R.string.definition_84_exploration) }
      85 -> { return getString(R.string.definition_85_expressiveness) }
      86 -> { return getString(R.string.definition_86_faith) }
      87 -> { return getString(R.string.definition_87_fairness) }
      88 -> { return getString(R.string.definition_88_fame) }
      89 -> { return getString(R.string.definition_89_favors) }
      90 -> { return getString(R.string.definition_90_fearlessness) }
      91 -> { return getString(R.string.definition_91_feelings) }
      92 -> { return getString(R.string.definition_92_ferocity) }
      93 -> { return getString(R.string.definition_93_fidelity) }
      94 -> { return getString(R.string.definition_94_flexibility) }
      95 -> { return getString(R.string.definition_95_focus) }
      96 -> { return getString(R.string.definition_96_foresight) }
      97 -> { return getString(R.string.definition_97_forgiveness) }
      98 -> { return getString(R.string.definition_98_fortitude) }
      99 -> { return getString(R.string.definition_99_freedom) }
      100 -> { return getString(R.string.definition_100_friendship) }
      101 -> { return getString(R.string.definition_101_generosity) }
      102 -> { return getString(R.string.definition_102_genius) }
      103 -> { return getString(R.string.definition_103_giving) }
      104 -> { return getString(R.string.definition_104_goodness) }
      105 -> { return getString(R.string.definition_105_grace) }
      106 -> { return getString(R.string.definition_106_gratitude) }
      107 -> { return getString(R.string.definition_107_greatness) }
      108 -> { return getString(R.string.definition_108_growth) }
      109 -> { return getString(R.string.definition_109_guidance) }
      110 -> { return getString(R.string.definition_110_happiness) }
      111 -> { return getString(R.string.definition_111_hard_work) }
      112 -> { return getString(R.string.definition_112_harmony) }
      113 -> { return getString(R.string.definition_113_health) }
      114 -> { return getString(R.string.definition_114_helping_friends) }
      115 -> { return getString(R.string.definition_115_helping_strangers) }
      116 -> { return getString(R.string.definition_116_honesty) }
      117 -> { return getString(R.string.definition_117_honor) }
      118 -> { return getString(R.string.definition_118_hope) }
      119 -> { return getString(R.string.definition_119_hospitality) }
      120 -> { return getString(R.string.definition_120_humility) }
      121 -> { return getString(R.string.definition_121_imagination) }
      122 -> { return getString(R.string.definition_122_improvement) }
      123 -> { return getString(R.string.definition_123_independence) }
      124 -> { return getString(R.string.definition_124_individuality) }
      125 -> { return getString(R.string.definition_125_industriousness) }
      126 -> { return getString(R.string.definition_126_influence) }
      127 -> { return getString(R.string.definition_127_innovation) }
      128 -> { return getString(R.string.definition_128_inquisitiveness) }
      129 -> { return getString(R.string.definition_129_insight) }
      130 -> { return getString(R.string.definition_130_inspiration) }
      131 -> { return getString(R.string.definition_131_integrity) }
      132 -> { return getString(R.string.definition_132_intelligence) }
      133 -> { return getString(R.string.definition_133_intensity) }
      134 -> { return getString(R.string.definition_134_intimacy) }
      135 -> { return getString(R.string.definition_135_intuition) }
      136 -> { return getString(R.string.definition_136_inventiveness) }
      137 -> { return getString(R.string.definition_137_joy) }
      138 -> { return getString(R.string.definition_138_justice) }
      139 -> { return getString(R.string.definition_139_kindness) }
      140 -> { return getString(R.string.definition_140_knowledge) }
      141 -> { return getString(R.string.definition_141_lawfulness) }
      142 -> { return getString(R.string.definition_142_leadership) }
      143 -> { return getString(R.string.definition_143_learning) }
      144 -> { return getString(R.string.definition_144_liberty) }
      145 -> { return getString(R.string.definition_145_logic) }
      146 -> { return getString(R.string.definition_146_love) }
      147 -> { return getString(R.string.definition_147_loyalty) }
      148 -> { return getString(R.string.definition_148_mastery) }
      149 -> { return getString(R.string.definition_149_maturity) }
      150 -> { return getString(R.string.definition_150_meaning) }
      151 -> { return getString(R.string.definition_151_moderation) }
      152 -> { return getString(R.string.definition_152_motivation) }
      153 -> { return getString(R.string.definition_153_openness) }
      154 -> { return getString(R.string.definition_154_optimism) }
      155 -> { return getString(R.string.definition_155_order) }
      156 -> { return getString(R.string.definition_156_organization) }
      157 -> { return getString(R.string.definition_157_originality) }
      158 -> { return getString(R.string.definition_158_passion) }
      159 -> { return getString(R.string.definition_159_patience) }
      160 -> { return getString(R.string.definition_160_peace) }
      161 -> { return getString(R.string.definition_161_performance) }
      162 -> { return getString(R.string.definition_162_persistence) }
      163 -> { return getString(R.string.definition_163_playfulness) }
      164 -> { return getString(R.string.definition_164_poise) }
      165 -> { return getString(R.string.definition_165_potential) }
      166 -> { return getString(R.string.definition_166_power) }
      167 -> { return getString(R.string.definition_167_presence) }
      168 -> { return getString(R.string.definition_168_productivity) }
      169 -> { return getString(R.string.definition_169_professionalism) }
      170 -> { return getString(R.string.definition_170_prosperity) }
      171 -> { return getString(R.string.definition_171_punctuality) }
      172 -> { return getString(R.string.definition_172_purpose) }
      173 -> { return getString(R.string.definition_173_rationality) }
      174 -> { return getString(R.string.definition_174_realism) }
      175 -> { return getString(R.string.definition_175_reason) }
      176 -> { return getString(R.string.definition_176_recognition) }
      177 -> { return getString(R.string.definition_177_recreation) }
      178 -> { return getString(R.string.definition_178_refinement) }
      179 -> { return getString(R.string.definition_179_reflection) }
      180 -> { return getString(R.string.definition_180_reliability) }
      181 -> { return getString(R.string.definition_181_resilience) }
      182 -> { return getString(R.string.definition_182_resolution) }
      183 -> { return getString(R.string.definition_183_resourcefulness) }
      184 -> { return getString(R.string.definition_184_respect) }
      185 -> { return getString(R.string.definition_185_responsibility) }
      186 -> { return getString(R.string.definition_186_responsiveness) }
      187 -> { return getString(R.string.definition_187_rest) }
      188 -> { return getString(R.string.definition_188_restraint) }
      189 -> { return getString(R.string.definition_189_reverence) }
      190 -> { return getString(R.string.definition_190_rigor) }
      191 -> { return getString(R.string.definition_191_risk_taking) }
      192 -> { return getString(R.string.definition_192_satisfaction) }
      193 -> { return getString(R.string.definition_193_security) }
      194 -> { return getString(R.string.definition_194_self_reliance) }
      195 -> { return getString(R.string.definition_195_self_awareness) }
      196 -> { return getString(R.string.definition_196_self_control) }
      197 -> { return getString(R.string.definition_197_selflessness) }
      198 -> { return getString(R.string.definition_198_sensitivity) }
      199 -> { return getString(R.string.definition_199_serenity) }
      200 -> { return getString(R.string.definition_200_service) }
      201 -> { return getString(R.string.definition_201_sharing) }
      202 -> { return getString(R.string.definition_202_silence) }
      203 -> { return getString(R.string.definition_203_simplicity) }
      204 -> { return getString(R.string.definition_204_sincerity) }
      205 -> { return getString(R.string.definition_205_skillfulness) }
      206 -> { return getString(R.string.definition_206_solitude) }
      207 -> { return getString(R.string.definition_207_spirituality) }
      208 -> { return getString(R.string.definition_208_spontaneity) }
      209 -> { return getString(R.string.definition_209_stability) }
      210 -> { return getString(R.string.definition_210_status) }
      211 -> { return getString(R.string.definition_211_stewardship) }
      212 -> { return getString(R.string.definition_212_strength) }
      213 -> { return getString(R.string.definition_213_structure) }
      214 -> { return getString(R.string.definition_214_success) }
      215 -> { return getString(R.string.definition_215_support) }
      216 -> { return getString(R.string.definition_216_surprise) }
      217 -> { return getString(R.string.definition_217_sustainability) }
      218 -> { return getString(R.string.definition_218_sympathy) }
      219 -> { return getString(R.string.definition_219_talent) }
      220 -> { return getString(R.string.definition_220_teamwork) }
      221 -> { return getString(R.string.definition_221_temperance) }
      222 -> { return getString(R.string.definition_222_thankfulness) }
      223 -> { return getString(R.string.definition_223_thoroughness) }
      224 -> { return getString(R.string.definition_224_thoughtfulness) }
      225 -> { return getString(R.string.definition_225_thrift) }
      226 -> { return getString(R.string.definition_226_timelessness) }
      227 -> { return getString(R.string.definition_227_tolerance) }
      228 -> { return getString(R.string.definition_228_toughness) }
      229 -> { return getString(R.string.definition_229_tradition) }
      230 -> { return getString(R.string.definition_230_tranquility) }
      231 -> { return getString(R.string.definition_231_transparency) }
      232 -> { return getString(R.string.definition_232_trust) }
      233 -> { return getString(R.string.definition_233_trustworthiness) }
      234 -> { return getString(R.string.definition_234_truth) }
      235 -> { return getString(R.string.definition_235_understanding) }
      236 -> { return getString(R.string.definition_236_uniqueness) }
      237 -> { return getString(R.string.definition_237_unity) }
      238 -> { return getString(R.string.definition_238_valor) }
      239 -> { return getString(R.string.definition_239_victory) }
      240 -> { return getString(R.string.definition_240_vigor) }
      241 -> { return getString(R.string.definition_241_vision) }
      242 -> { return getString(R.string.definition_242_vitality) }
      243 -> { return getString(R.string.definition_243_wealth) }
      244 -> { return getString(R.string.definition_244_welcoming) }
      245 -> { return getString(R.string.definition_245_wholeness) }
      246 -> { return getString(R.string.definition_246_wisdom) }
      247 -> { return getString(R.string.definition_247_wonder) }
      248 -> { return getString(R.string.definition_248_worthiness) }
      249 -> { return getString(R.string.definition_249_zeal) }

      else -> { return "Error: No Definition"}

    }

  }
}