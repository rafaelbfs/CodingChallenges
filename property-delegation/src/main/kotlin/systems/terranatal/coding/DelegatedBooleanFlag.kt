package systems.terranatal.coding

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


class DelegatedBooleanFlag<T>(private val bitSequenceProperty: KMutableProperty1<T, Int>, private val n: Int):
  ReadWriteProperty<T, Boolean> {
  /**
   *  Gets the Nth bit boolean value by calculating the mask which is `(1 << n)` meaning one shifted to the left in **n**
   *  positions. Then by doing `(mask ^ bitVector)` you will get the value of the n-th bit where anything different from 0 is true.
   */
  override operator fun getValue(thisRef: T, property: KProperty<*>): Boolean {
    val mask = 1 shl n
    val flags = bitSequenceProperty.get(thisRef)
    return flags and mask != 0 // flags & mask
  }

  /**
   * Sets the Nth bit which is done by, calculating the mask as in previous step
   * The numeric value **b** of the bit is 1 if the `value` argument is **true** 0 if **false**
   * The argument value a is formed by shifting b in n positions to the left
   * The current value of c is obtained by `mask ^ flags`
   * Then we perform eXclusive-OR `XOR` on a and c -> `a xor c`
   * The new flag value, result R of the previous operation is then set to bit vector by simply calculating `R xor flags`
   */
  override operator fun setValue(thisRef: T, property: KProperty<*>, value: Boolean) {
    val flags = bitSequenceProperty.get(thisRef)
    val mask = 1 shl n
    var bit = if (value) 1 else 0
    bit = bit shl n
    var newFlagValue = bit xor (mask and flags) // 0 X 1  -> 1 X 1 -> 0
    bitSequenceProperty.set(thisRef, newFlagValue xor flags)
  }

}