package org.clarkecollective.raderie.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.google.firebase.auth.FirebaseUser
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.api.FirebaseAPI
import org.clarkecollective.raderie.daos.ValueDao
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.databinding.ActivityMainBinding
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

    valueDao =
      Room.databaseBuilder(applicationContext, MyValuesDatabase::class.java, "test-db").build()
        .valueDao()

    Logger.d("Files dir: %s", filesDir)

    firebaseAPI.logInAndReturnUser().subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<FirebaseUser> {
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
      })
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

  private fun startSharingActivity() {
    val intent = ShareActivity.newIntent(this)
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
      } else {
        Toast.makeText(applicationContext, "Deleted: " + hv2.name, Toast.LENGTH_SHORT).show()
      }
      mainActivityViewModel.pullTwo()
    }
    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, hv2!!.name) { _, _ ->
      val success = mainActivityViewModel.removeFromDeck(hv1)
      if (!success) {
        Toast.makeText(applicationContext, "Item Not Found", Toast.LENGTH_SHORT).show()
      } else {
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
    return when (id) {
      -2 -> getString(R.string.synonym_explanation)
      -1 -> getString(R.string.tie_explanation)
      0 -> getString(R.string.definition_0_acceptance)
      1 -> getString(R.string.definition_1_accomplishment)
      2 -> getString(R.string.definition_2_accountability)
      3 -> getString(R.string.definition_3_accuracy)
      4 -> getString(R.string.definition_4_achievement)
      5 -> getString(R.string.definition_5_adaptability)
      6 -> getString(R.string.definition_6_alertness)
      7 -> getString(R.string.definition_7_altruism)
      8 -> getString(R.string.definition_8_ambition)
      9 -> getString(R.string.definition_9_amusement)
      10 -> getString(R.string.definition_10_assertiveness)
      11 -> getString(R.string.definition_11_attentiveness)
      12 -> getString(R.string.definition_12_awareness)
      13 -> getString(R.string.definition_13_balance)
      14 -> getString(R.string.definition_14_beauty)
      15 -> getString(R.string.definition_15_boldness)
      16 -> getString(R.string.definition_16_bravery)
      17 -> getString(R.string.definition_17_brilliance)
      18 -> getString(R.string.definition_18_calm)
      19 -> getString(R.string.definition_19_candor)
      20 -> getString(R.string.definition_20_capability)
      21 -> getString(R.string.definition_21_carefulness)
      22 -> getString(R.string.definition_22_certainty)
      23 -> getString(R.string.definition_23_challenge)
      24 -> getString(R.string.definition_24_charisma)
      25 -> getString(R.string.definition_25_charity)
      26 -> getString(R.string.definition_26_cheerfulness)
      27 -> getString(R.string.definition_27_clarity)
      28 -> getString(R.string.definition_28_cleanliness)
      29 -> getString(R.string.definition_29_clear_headedness)
      30 -> getString(R.string.definition_30_cleverness)
      31 -> getString(R.string.definition_31_comfort)
      32 -> getString(R.string.definition_32_commitment)
      33 -> getString(R.string.definition_33_communication)
      34 -> getString(R.string.definition_34_community)
      35 -> getString(R.string.definition_35_compassion)
      36 ->  getString(R.string.definition_36_competence)
      37 ->  getString(R.string.definition_37_composure)
      38 ->  getString(R.string.definition_38_concentration)
      39 ->  getString(R.string.definition_39_confidence)
      40 ->  getString(R.string.definition_40_conformity)
      41 ->  getString(R.string.definition_41_consciousness)
      42 ->  getString(R.string.definition_42_connection)
      43 ->  getString(R.string.definition_43_consciousness)
      44 ->  getString(R.string.definition_44_consideration)
      45 ->  getString(R.string.definition_45_consistency)
      46 ->  getString(R.string.definition_46_contentment)
      47 ->  getString(R.string.definition_47_contribution)
      48 ->  getString(R.string.definition_48_control)
      49 ->  getString(R.string.definition_49_conviction)
      50 ->  getString(R.string.definition_50_cooperation)
      51 ->  getString(R.string.definition_51_courage)
      52 ->  getString(R.string.definition_52_courtesy)
      53 ->  getString(R.string.definition_53_creativity)
      54 ->  getString(R.string.definition_54_credibility)
      55 ->  getString(R.string.definition_55_curiosity)
      56 ->  getString(R.string.definition_56_decisiveness)
      57 ->  getString(R.string.definition_57_decorum)
      58 ->  getString(R.string.definition_58_dedication)
      59 ->  getString(R.string.definition_59_dependability)
      60 ->  getString(R.string.definition_60_determination)
      61 ->  getString(R.string.definition_61_development)
      62 ->  getString(R.string.definition_62_devotion)
      63 ->  getString(R.string.definition_63_dignity)
      64 ->  getString(R.string.definition_64_discipline)
      65 ->  getString(R.string.definition_65_discovery)
      66 ->  getString(R.string.definition_66_dreams)
      67 ->  getString(R.string.definition_67_drive)
      68 ->  getString(R.string.definition_68_duty)
      69 ->  getString(R.string.definition_69_earnestness)
      70 ->  getString(R.string.definition_70_effectiveness)
      71 ->  getString(R.string.definition_71_efficiency)
      72 ->  getString(R.string.definition_72_elegance)
      73 ->  getString(R.string.definition_73_empathy)
      74 ->  getString(R.string.definition_74_endurance)
      75 ->  getString(R.string.definition_75_energy)
      76 ->  getString(R.string.definition_76_enjoyment)
      77 ->  getString(R.string.definition_77_enthusiasm)
      78 ->  getString(R.string.definition_78_equity)
      79 ->  getString(R.string.definition_79_ethics)
      80 ->  getString(R.string.definition_80_excellence)
      81 ->  getString(R.string.definition_81_excitement)
      82 ->  getString(R.string.definition_82_experience)
      83 ->  getString(R.string.definition_83_exercise)
      84 ->  getString(R.string.definition_84_exploration)
      85 ->  getString(R.string.definition_85_expressiveness)
      86 ->  getString(R.string.definition_86_faith)
      87 ->  getString(R.string.definition_87_fairness)
      88 ->  getString(R.string.definition_88_fame)
      89 ->  getString(R.string.definition_89_favors)
      90 ->  getString(R.string.definition_90_fearlessness)
      91 ->  getString(R.string.definition_91_feelings)
      92 ->  getString(R.string.definition_92_ferocity)
      93 ->  getString(R.string.definition_93_fidelity)
      94 ->  getString(R.string.definition_94_flexibility)
      95 ->  getString(R.string.definition_95_focus)
      96 ->  getString(R.string.definition_96_foresight)
      97 ->  getString(R.string.definition_97_forgiveness)
      98 ->  getString(R.string.definition_98_fortitude)
      99 ->  getString(R.string.definition_99_freedom)
      100 ->  getString(R.string.definition_100_friendship)
      101 ->  getString(R.string.definition_101_generosity)
      102 ->  getString(R.string.definition_102_genius)
      103 ->  getString(R.string.definition_103_giving)
      104 ->  getString(R.string.definition_104_goodness)
      105 ->  getString(R.string.definition_105_grace)
      106 ->  getString(R.string.definition_106_gratitude)
      107 ->  getString(R.string.definition_107_greatness)
      108 ->  getString(R.string.definition_108_growth)
      109 ->  getString(R.string.definition_109_guidance)
      110 ->  getString(R.string.definition_110_happiness)
      111 ->  getString(R.string.definition_111_hard_work)
      112 ->  getString(R.string.definition_112_harmony)
      113 ->  getString(R.string.definition_113_health)
      114 ->  getString(R.string.definition_114_helping_friends)
      115 ->  getString(R.string.definition_115_helping_strangers)
      116 ->  getString(R.string.definition_116_honesty)
      117 ->  getString(R.string.definition_117_honor)
      118 ->  getString(R.string.definition_118_hope)
      119 ->  getString(R.string.definition_119_hospitality)
      120 ->  getString(R.string.definition_120_humility)
      121 ->  getString(R.string.definition_121_imagination)
      122 ->  getString(R.string.definition_122_improvement)
      123 ->  getString(R.string.definition_123_independence)
      124 ->  getString(R.string.definition_124_individuality)
      125 ->  getString(R.string.definition_125_industriousness)
      126 ->  getString(R.string.definition_126_influence)
      127 ->  getString(R.string.definition_127_innovation)
      128 ->  getString(R.string.definition_128_inquisitiveness)
      129 ->  getString(R.string.definition_129_insight)
      130 ->  getString(R.string.definition_130_inspiration)
      131 ->  getString(R.string.definition_131_integrity)
      132 ->  getString(R.string.definition_132_intelligence)
      133 ->  getString(R.string.definition_133_intensity)
      134 ->  getString(R.string.definition_134_intimacy)
      135 ->  getString(R.string.definition_135_intuition)
      136 ->  getString(R.string.definition_136_inventiveness)
      137 ->  getString(R.string.definition_137_joy)
      138 ->  getString(R.string.definition_138_justice)
      139 ->  getString(R.string.definition_139_kindness)
      140 ->  getString(R.string.definition_140_knowledge)
      141 ->  getString(R.string.definition_141_lawfulness)
      142 ->  getString(R.string.definition_142_leadership)
      143 ->  getString(R.string.definition_143_learning)
      144 ->  getString(R.string.definition_144_liberty)
      145 ->  getString(R.string.definition_145_logic)
      146 ->  getString(R.string.definition_146_love)
      147 ->  getString(R.string.definition_147_loyalty)
      148 ->  getString(R.string.definition_148_mastery)
      149 ->  getString(R.string.definition_149_maturity)
      150 ->  getString(R.string.definition_150_meaning)
      151 ->  getString(R.string.definition_151_moderation)
      152 ->  getString(R.string.definition_152_motivation)
      153 ->  getString(R.string.definition_153_openness)
      154 ->  getString(R.string.definition_154_optimism)
      155 ->  getString(R.string.definition_155_order)
      156 ->  getString(R.string.definition_156_organization)
      157 ->  getString(R.string.definition_157_originality)
      158 ->  getString(R.string.definition_158_passion)
      159 ->  getString(R.string.definition_159_patience)
      160 ->  getString(R.string.definition_160_peace)
      161 ->  getString(R.string.definition_161_performance)
      162 ->  getString(R.string.definition_162_persistence)
      163 ->  getString(R.string.definition_163_playfulness)
      164 ->  getString(R.string.definition_164_poise)
      165 ->  getString(R.string.definition_165_potential)
      166 ->  getString(R.string.definition_166_power)
      167 ->  getString(R.string.definition_167_presence)
      168 ->  getString(R.string.definition_168_productivity)
      169 ->  getString(R.string.definition_169_professionalism)
      170 ->  getString(R.string.definition_170_prosperity)
      171 ->  getString(R.string.definition_171_punctuality)
      172 ->  getString(R.string.definition_172_purpose)
      173 ->  getString(R.string.definition_173_rationality)
      174 ->  getString(R.string.definition_174_realism)
      175 ->  getString(R.string.definition_175_reason)
      176 ->  getString(R.string.definition_176_recognition)
      177 ->  getString(R.string.definition_177_recreation)
      178 ->  getString(R.string.definition_178_refinement)
      179 ->  getString(R.string.definition_179_reflection)
      180 ->  getString(R.string.definition_180_reliability)
      181 ->  getString(R.string.definition_181_resilience)
      182 ->  getString(R.string.definition_182_resolution)
      183 ->  getString(R.string.definition_183_resourcefulness)
      184 ->  getString(R.string.definition_184_respect)
      185 ->  getString(R.string.definition_185_responsibility)
      186 ->  getString(R.string.definition_186_responsiveness)
      187 ->  getString(R.string.definition_187_rest)
      188 ->  getString(R.string.definition_188_restraint)
      189 ->  getString(R.string.definition_189_reverence)
      190 ->  getString(R.string.definition_190_rigor)
      191 ->  getString(R.string.definition_191_risk_taking)
      192 ->  getString(R.string.definition_192_satisfaction)
      193 ->  getString(R.string.definition_193_security)
      194 ->  getString(R.string.definition_194_self_reliance)
      195 ->  getString(R.string.definition_195_self_awareness)
      196 ->  getString(R.string.definition_196_self_control)
      197 ->  getString(R.string.definition_197_selflessness)
      198 ->  getString(R.string.definition_198_sensitivity)
      199 ->  getString(R.string.definition_199_serenity)
      200 ->  getString(R.string.definition_200_service)
      201 ->  getString(R.string.definition_201_sharing)
      202 ->  getString(R.string.definition_202_silence)
      203 ->  getString(R.string.definition_203_simplicity)
      204 ->  getString(R.string.definition_204_sincerity)
      205 ->  getString(R.string.definition_205_skillfulness)
      206 ->  getString(R.string.definition_206_solitude)
      207 ->  getString(R.string.definition_207_spirituality)
      208 ->  getString(R.string.definition_208_spontaneity)
      209 ->  getString(R.string.definition_209_stability)
      210 ->  getString(R.string.definition_210_status)
      211 ->  getString(R.string.definition_211_stewardship)
      212 ->  getString(R.string.definition_212_strength)
      213 ->  getString(R.string.definition_213_structure)
      214 ->  getString(R.string.definition_214_success)
      215 ->  getString(R.string.definition_215_support)
      216 ->  getString(R.string.definition_216_surprise)
      217 ->  getString(R.string.definition_217_sustainability)
      218 ->  getString(R.string.definition_218_sympathy)
      219 ->  getString(R.string.definition_219_talent)
      220 ->  getString(R.string.definition_220_teamwork)
      221 ->  getString(R.string.definition_221_temperance)
      222 ->  getString(R.string.definition_222_thankfulness)
      223 ->  getString(R.string.definition_223_thoroughness)
      224 ->  getString(R.string.definition_224_thoughtfulness)
      225 ->  getString(R.string.definition_225_thrift)
      226 ->  getString(R.string.definition_226_timelessness)
      227 ->  getString(R.string.definition_227_tolerance)
      228 ->  getString(R.string.definition_228_toughness)
      229 ->  getString(R.string.definition_229_tradition)
      230 ->  getString(R.string.definition_230_tranquility)
      231 ->  getString(R.string.definition_231_transparency)
      232 ->  getString(R.string.definition_232_trust)
      233 ->  getString(R.string.definition_233_trustworthiness)
      234 ->  getString(R.string.definition_234_truth)
      235 ->  getString(R.string.definition_235_understanding)
      236 ->  getString(R.string.definition_236_uniqueness)
      237 ->  getString(R.string.definition_237_unity)
      238 ->  getString(R.string.definition_238_valor)
      239 ->  getString(R.string.definition_239_victory)
      240 ->  getString(R.string.definition_240_vigor)
      241 ->  getString(R.string.definition_241_vision)
      242 ->  getString(R.string.definition_242_vitality)
      243 ->  getString(R.string.definition_243_wealth)
      244 ->  getString(R.string.definition_244_welcoming)
      245 ->  getString(R.string.definition_245_wholeness)
      246 ->  getString(R.string.definition_246_wisdom)
      247 ->  getString(R.string.definition_247_wonder)
      248 ->  getString(R.string.definition_248_worthiness)
      249 ->  getString(R.string.definition_249_zeal)
      else ->  "Error: No Definition"
    }
  }

  override fun onDestroy() {
    firebaseAPI.dispose()
    super.onDestroy()

  }
}
