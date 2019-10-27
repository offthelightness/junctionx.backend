package com.passangers.junctionx.backend.service

import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.GeoPoint
import com.passangers.junctionx.backend.model.LoadLevel
import com.passangers.junctionx.backend.repo.AtmLoadRepository
import com.passangers.junctionx.backend.repo.AtmRepository
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

    fun getAtms(
        searchArea: SearchArea,
        userLocation: GeoPoint?,
        canDeposit: Boolean?,
        usePrediction : Boolean?
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
            return if (usePrediction == null || usePrediction == false ) {
                val closestAtm = atmOutputDataList.firstOrNull()
                AtmSearchResult(
                    closestAtm,
                    atmOutputDataList
                )
            } else {

                AtmSearchResult(
                    null,
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
        val realtimeWaitingTime = predictionService.getAverageHistoricalWaitingTime(atm.id)

        return AtmOutputData(atm, loadLevel, lineDistanceInMeters, realDistanceInMeters, averageHistoricalWaitingTime, 0.0)
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