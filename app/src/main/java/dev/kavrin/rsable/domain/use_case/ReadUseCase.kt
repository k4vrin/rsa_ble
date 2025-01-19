package dev.kavrin.rsable.domain.use_case

import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import java.util.UUID

class ReadUseCase(
    private val bleRepository: BleRepository
) {
    suspend operator fun invoke(
        characteristicUuid: String
    ): Resource<ByteArray, GattEvent.Error> {
        return bleRepository.readCharacteristic(UUID.fromString(characteristicUuid))
    }
}

