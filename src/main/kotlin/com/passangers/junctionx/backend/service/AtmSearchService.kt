package com.passangers.junctionx.backend.service

import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.GeoPoint
import com.passangers.junctionx.backend.model.LoadLevel
import com.passangers.junctionx.backend.repo.AtmIntentRepository
import com.passangers.junctionx.backend.repo.AtmLoadRepository
import com.passangers.junctionx.backend.repo.AtmRepository
import com.passangers.junctionx.backend.service.PredictionService.Companion.ADDITIONAL_SEARCH_RADIUS_IN_METERS
import com.passangers.junctionx.backend.service.PredictionService.Companion.AVERAGE_PEDESTRIAN_SPEED
import com.passangers.junctionx.backend.service.PredictionService.Companion.AVERAGE_SESSION_TIME_IN_MINUTES
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AtmSearchService {

    @Autowired
    lateinit var atmRepository: AtmRepository

    @Autowired
    lateinit var atmLoadRepository: AtmLoadRepository

    @Autowired
    lateinit var geoService: GeoService

    @Autowired
    lateinit var predictionService: PredictionService

    @Autowired
    lateinit var atmIntentRepository: AtmIntentRepository

    fun getAtms(
        searchArea: SearchArea,
        userLocation: GeoPoint?,
        canDeposit: Boolean?,
        usePrediction: Boolean?
    ): AtmSearchResult {
        val findAtms = findAtms(searchArea, canDeposit)

        val atmOutputDataList = findAtms.map {
            convertToAtmOutputData(it, userLocation)
        }.sortedBy { it.lineDistanceInMeters ?: 0.0 }

        if (userLocation == null) {
            return AtmSearchResult(
                null,
                atmOutputDataList
            )
        } else {
            val closestAtm = atmOutputDataList.firstOrNull()
            val distanceBetweenUserAndClosestATM = closestAtm?.lineDistanceInMeters?: 0.0
            return if (usePrediction == null || usePrediction == false) {
                AtmSearchResult(
                    closestAtm,
                    atmOutputDataList
                )
            } else {
                val potentialGoodATMs = atmOutputDataList.filter {
                   (it.lineDistanceInMeters?.minus(distanceBetweenUserAndClosestATM) ?: Double.MAX_VALUE) < ADDITIONAL_SEARCH_RADIUS_IN_METERS
                }

                val potentialGoodATMsWithScore =  potentialGoodATMs.map {
                  return@map  AtmWithScore(it,
                        it.realDistanceInMeters?.div(AVERAGE_PEDESTRIAN_SPEED) ?: Double.MAX_VALUE
                                +  predictionService.getAverageHistoricalWaitingTime(it.atm.id)
                                +  atmIntentRepository.findCountOfIntentForATM(it.atm.id) * AVERAGE_SESSION_TIME_IN_MINUTES)
                }.sortedBy {
                    it.score
                }

                potentialGoodATMsWithScore.forEach {
                    println(it)
                }

                AtmSearchResult(
                    potentialGoodATMsWithScore.firstOrNull()?.atmOutputData,
                    atmOutputDataList
                )
            }
        }
    }

    private fun findAtms(
        searchArea: SearchArea,
        canDeposit: Boolean?
    ): List<Atm> {
        return atmRepository.findAll().filter {
            if (it.geoX < searchArea.south
                || it.geoX > searchArea.nord
                || it.geoY < searchArea.west
                || it.geoY > searchArea.east
            ) {
                return@filter false
            }
            if (canDeposit != null) {
                if (it.canDeposit != canDeposit) {
                    return@filter false
                }
            }
            return@filter true
        }
    }

    private fun convertToAtmOutputData(atm: Atm, userLocation: GeoPoint?): AtmOutputData {
        val now = LocalDateTime.now()
        val atmLoad = atmLoadRepository.findByAtmIDForConcreteDayAndPeriod(
            atm.id, now.dayOfWeek,
            now.toLocalTime().toSecondOfDay().toLong(),
            now.toLocalTime().toSecondOfDay().toLong()
        ).firstOrNull()
        val atmLoadAbsoluteValue = atmLoad?.transactionsCount ?: 0

        val loadLevel = when {
            atmLoadAbsoluteValue < 10 -> LoadLevel.EMPTY
            atmLoadAbsoluteValue < 20 -> LoadLevel.LEVEL_1
            atmLoadAbsoluteValue < 30 -> LoadLevel.LEVEL_2
            atmLoadAbsoluteValue < 40 -> LoadLevel.LEVEL_3
            else -> LoadLevel.LEVEL_4
        }
        val lineDistanceInMeters = if (userLocation != null) {
            geoService.getLineDistance(userLocation, GeoPoint(atm.geoX, atm.geoY))
        } else {
            null
        }

        val realDistanceInMeters = if (userLocation != null) {
            geoService.getRealDistance(userLocation, GeoPoint(atm.geoX, atm.geoY))
        } else {
            null
        }

        val averageHistoricalWaitingTime = predictionService.getAverageHistoricalWaitingTime(atm.id)
        val realtimeWaitingTime = atmIntentRepository.findCountOfIntentForATM(atm.id).toDouble() * AVERAGE_SESSION_TIME_IN_MINUTES

        return AtmOutputData(
            atm,
            loadLevel,
            lineDistanceInMeters,
            realDistanceInMeters,
            averageHistoricalWaitingTime,
            realtimeWaitingTime
        )
    }
}

data class AtmSearchResult(
    val bestAtm: AtmOutputData?,
    val items: List<AtmOutputData>
)

data class AtmOutputData(
    val atm: Atm,
    val loadLevel: LoadLevel,
    val lineDistanceInMeters: Double?,
    val realDistanceInMeters: Double?,
    val averageHistoricalWaitingTime: Double?,
    val realtimeWaitingTime: Double?
)

data class SearchArea(
    val nord: Double,
    val south: Double,
    val east: Double,
    val west: Double
)

data class AtmWithScore(
    val atmOutputData: AtmOutputData,
    val score: Double
)