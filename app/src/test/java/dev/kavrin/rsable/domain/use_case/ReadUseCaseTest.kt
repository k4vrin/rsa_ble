package dev.kavrin.rsable.domain.use_case

import dev.kavrin.rsable.domain.model.GattCharacteristic
import dev.kavrin.rsable.domain.model.GattEvent
import dev.kavrin.rsable.domain.model.Resource
import dev.kavrin.rsable.domain.repository.BleRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.UUID

class ReadUseCaseTest {

    private lateinit var bleRepository: BleRepository
    private lateinit var readUseCase: ReadUseCase

    @Before
    fun setUp() {
        bleRepository = mock(BleRepository::class.java)
        readUseCase = ReadUseCase(bleRepository)
    }

    @Test
    fun `invoke should return success result when repository returns data`(): Unit = runBlocking {

        val characteristicUuid = "00002a37-0000-1000-8000-00805f9b34fb"
        val expectedData = byteArrayOf(0x01, 0x02, 0x03)
        val expectedDataFake = byteArrayOf(0x01, 0x03, 0x03)
        val successResource = Resource.Success(expectedData)

        `when`(bleRepository.readCharacteristic(UUID.fromString(characteristicUuid)))
            .thenReturn(successResource)

        val result = readUseCase(characteristicUuid)

//        assertEquals(Resource.success(expectedDataFake), result)
        assertEquals(successResource, result)
        verify(bleRepository).readCharacteristic(UUID.fromString(characteristicUuid))
    }

    @Test
    fun `invoke should return error result when repository returns error`(): Unit = runBlocking {
        val characteristicUuid = "00002a37-0000-1000-8000-00805f9b34fb"
        val errorEvent = GattEvent.Error.GattError(
            status = -999,
            operation = "onCharacteristicRead",
            message = "Characteristic read failed"
        )
        val resource = Resource.error(errorEvent)
        val fakeResource = Resource.error(
            GattEvent.Error.ConnectionLost()
        )

        `when`(bleRepository.readCharacteristic(UUID.fromString(characteristicUuid)))
            .thenReturn(resource)

        // Act
        val result = readUseCase(characteristicUuid)

        // Assert
        assertEquals(resource, result)
        verify(bleRepository).readCharacteristic(UUID.fromString(characteristicUuid))
    }
}
