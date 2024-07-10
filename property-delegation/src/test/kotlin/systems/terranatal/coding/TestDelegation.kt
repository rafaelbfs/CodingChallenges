package systems.terranatal.coding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class TestDelegation {

  data class TestClass(var flags: Int) {
    var bit0 by DelegatedBooleanFlag(TestClass::flags, 0)
    var bit4 by DelegatedBooleanFlag(TestClass::flags, 4)
    var bit24 by DelegatedBooleanFlag(TestClass::flags, 24)
    var errorXPresent by DelegatedBooleanFlag(TestClass::flags, 30)
  }


  @Test fun testDelegation() {
    var bits = TestClass(0)
    assertFalse(bits.bit0)
    assertFalse(bits.bit4)
    assertFalse(bits.bit24)

    bits.bit0 = true
    assertEquals(1, bits.flags)
    bits.bit4 = true
    assertEquals(0x11, bits.flags)

    bits.bit0 = false
    assertEquals(0x10, bits.flags)
    bits.bit24 = true
    assertEquals((1 shl 24) xor 0x10, bits.flags)
  }
}