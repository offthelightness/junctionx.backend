package com.passangers.junctionx.backend.service

import com.passangers.junctionx.backend.model.Atm
import com.passangers.junctionx.backend.repo.AtmLoadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class PredictionService {

    companion object {
        val AVERAGE_SESSION_TIME_IN_MINUTES = 3
        val AVERAGE_PEDESTRIAN_SPEED = 5000 / 60
        val STATISTICS_DAYS = 4
        val MINUTES_IN_STATISTICS_SLOT = 30
    }

    @Autowired
    lateinit var atmLoadRepository: AtmLoadRepository

    /**
     * ATM Model (M/M/I Markov chain’s model )
     * */
    fun getAverageHistoricalWaitingTime(ATMid: UUID): Double {
        val now = LocalDateTime.now()
        val atmLoad = atmLoadRepository.findByAtmIDForConcreteDayAndPeriod(
            ATMid, now.dayOfWeek,
            now.toLocalTime().toSecondOfDay().toLong(),
            now.toLocalTime().toSecondOfDay().toLong()
        ).firstOrNull()

    return if (atmLoad != null) {

            val requestStream : Double = atmLoad.transactionsCount.toDouble() / STATISTICS_DAYS / MINUTES_IN_STATISTICS_SLOT
            val serviceStream : Double = 1.0 / AVERAGE_SESSION_TIME_IN_MINUTES

            val utilizationFactor = requestStream / serviceStream

            AVERAGE_SESSION_TIME_IN_MINUTES * utilizationFactor / (1 - utilizationFactor)
        } else {
            0.0
        }
    }
}