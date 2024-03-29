package com.passangers.junctionx.backend.endpoint

import com.passangers.junctionx.backend.model.ATMIntent
import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.model.GeoPoint
import com.passangers.junctionx.backend.repo.AtmIntentRepository
import com.passangers.junctionx.backend.repo.AtmLoadRepository
import com.passangers.junctionx.backend.repo.AtmRepository
import com.passangers.junctionx.backend.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

@RestController
class AtmController {

    @Autowired
    lateinit var atmRepository: AtmRepository
    @Autowired
    lateinit var atmLoadRepository: AtmLoadRepository

    @Autowired
    lateinit var geoService: GeoService

    @Autowired
    lateinit var atmSearchService: AtmSearchService

    @Autowired
    lateinit var atmIntentRepository: AtmIntentRepository

    @GetMapping("atm")
    fun getAtms(
        @RequestParam("location", required = false)
        location: String?,
        @RequestParam("ne", required = true)
        ne: String,
        @RequestParam("sw", required = true)
        sw: String,
        @RequestParam("canDeposit", required = false)
        canDeposit: Boolean?
    ): ResponseEntity<GetAtmResponse> {
        val itemsMap = HashMap<String, Atm>()
        File("/Users/avanisimov/Downloads/atm_201909_IX_eng.txt")
            .forEachLine(Charset.forName("windows-1252")) { line ->
                val dataArray = line.split('\t')
                if (dataArray[4] != "STREET_ADDRESS") {
                    itemsMap.put(
                        dataArray[4],
                        Atm(
                            id = UUID.randomUUID(),
                            city = dataArray[3],
                            zipCD = dataArray[2],
                            address = dataArray[4],
                            geoX = dataArray[5].replace(',', '.').toDouble(),
                            geoY = dataArray[6].replace(',', '.').toDouble(),
                            canDeposit = dataArray[1] == "Y"
                        )
                    )
                }
            }

        val items = itemsMap.values.toList()

        val nord = ne.split(',')[0].toDouble()
        val east = ne.split(',')[1].toDouble()
        val south = sw.split(',')[0].toDouble()
        val west = sw.split(',')[1].toDouble()

        val filteredItems = items.filter {
            if (it.geoX < south
                || it.geoX > nord
                || it.geoY < west
                || it.geoY > east
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
        return ResponseEntity.ok(
            GetAtmResponse(
                filteredItems.size,
                filteredItems
            )
        )
    }

    @GetMapping("atm/v2")
    fun getAtmsV2(
        @RequestParam("location", required = false)
        location: String?,
        @RequestParam("ne", required = true)
        ne: String,
        @RequestParam("sw", required = true)
        sw: String,
        @RequestParam("canDeposit", required = false)
        canDeposit: Boolean?
    ): ResponseEntity<GetAtmResponseWithDistance> {

        val atmsFindAll = atmRepository.findAll()
        val itemsMap = HashMap<String, Atm>()
        File("/Users/avanisimov/Downloads/atm_201909_IX_eng.txt")
            .forEachLine(Charset.forName("windows-1252")) { line ->
                val dataArray = line.split('\t')
                if (dataArray[4] != "STREET_ADDRESS") {
//                    val targetAtm = atmsFindAll.find {
//                        it.address == dataArray[4]
//                    }
                    itemsMap.put(
                        dataArray[4],
                        Atm(
                            id = UUID.randomUUID(),
                            city = dataArray[3],
                            zipCD = dataArray[2],
                            address = dataArray[4],
                            geoX = dataArray[5].replace(',', '.').toDouble(),
                            geoY = dataArray[6].replace(',', '.').toDouble(),
                            canDeposit = dataArray[1] == "Y"
                        )
                    )
                }
            }

        val items = itemsMap.values.toList()

        val nord = ne.split(',')[0].toDouble()
        val east = ne.split(',')[1].toDouble()
        val south = sw.split(',')[0].toDouble()
        val west = sw.split(',')[1].toDouble()

        val userLocation = GeoPoint(47.478571, 19.087598)

        val filteredItems = items.filter {
            if (it.geoX < south
                || it.geoX > nord
                || it.geoY < west
                || it.geoY > east
            ) {
                return@filter false
            }
            if (canDeposit != null) {
                if (it.canDeposit != canDeposit) {
                    return@filter false
                }
            }
            return@filter true
        }.map {
            AtmWithDistance(
                it,
                geoService.getLineDistance(
                    userLocation, GeoPoint(
                        it.geoX, it.geoY
                    )
                )
            )
        }.sortedBy { it.distanceInMeters }
        return ResponseEntity.ok(
            GetAtmResponseWithDistance(
                filteredItems.size,
                filteredItems
            )
        )
    }

    @GetMapping("atm/v3")
    fun getAtmsV3(
        @RequestParam("location", required = false)
        location: String?,
        @RequestParam("ne", required = true)
        ne: String,
        @RequestParam("sw", required = true)
        sw: String,
        @RequestParam("canDeposit", required = false)
        canDeposit: Boolean?,
        @RequestParam("usePrediction", required = false)
        usePrediction: Boolean?
    ): ResponseEntity<AtmSearchResult> {
        val userLocation = if (location == null) {
            null
        } else {
            GeoPoint(
                location.split(',')[0].toDouble(),
                location.split(',')[1].toDouble()
            )
        }
        val atmSearchResult = atmSearchService.getAtms(
            SearchArea(
                ne.split(',')[0].toDouble(),
                sw.split(',')[0].toDouble(),
                ne.split(',')[1].toDouble(),
                sw.split(',')[1].toDouble()
            ), userLocation, canDeposit, usePrediction
        )
        return ResponseEntity.ok(
            atmSearchResult
        )
    }

    @PostMapping("atm/intent/v1")
    fun postATMIntent(
        @RequestBody(required = false)
        atmIntent: ATMIntent?
    ): ResponseEntity<Any> {
        atmIntent?.let {
            atmIntentRepository.save(it)
        }
        return ResponseEntity.ok(
            Any()
        )
    }
}

data class GetAtmResponse(
    val total: Int,
    val items: List<Atm>
)

data class GetAtmResponseWithDistance(
    val total: Int,
    val items: List<AtmWithDistance>
)


data class AtmWithDistance(
    val atm: Atm,
    val distanceInMeters: Double
)

