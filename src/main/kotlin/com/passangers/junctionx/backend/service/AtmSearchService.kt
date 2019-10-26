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

    fun getAtms(
        searchArea: SearchArea,
        userLocation: GeoPoint?,
        canDeposit: Boolean?
    ): AtmSearchResult {
        val findAtms = findAtms(searchArea, canDeposit)

        val atmOutputDataList = findAtms.map {
            convertToAtmOutputData(it, userLocation)
        }.sortedBy { it.distanceInMeters ?: 0.0 }

        val closestAtm = atmOutputDataList.firstOrNull()
//        val furthestAtm = atmOutputDataList.last()
//
//        val bestAtm = atmOutputDataList.map {
//            calculateScore(it)
//        }.sortedBy { it.score }.firstOrNull()
        return AtmSearchResult(
            closestAtm,
            atmOutputDataList
        )
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
            atm.id,now.dayOfWeek,
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
        val distanceInMeters = if (userLocation == null) {
            null
        } else {
            geoService.getSimpleDistance(userLocation, GeoPoint(atm.geoX, atm.geoY))
        }
        return AtmOutputData(atm, loadLevel, distanceInMeters)
    }
}

data class AtmSearchResult(
    val bestAtm: AtmOutputData?,
    val items: List<AtmOutputData>
)

data class AtmWithScore(
    val atmOutputData: AtmOutputData,
    val score: Double
)

data class AtmOutputData(
    val atm: Atm,
    val loadLevel: LoadLevel,
    val distanceInMeters: Double?
)

data class SearchArea(
    val nord: Double,
    val south: Double,
    val east: Double,
    val west: Double
)