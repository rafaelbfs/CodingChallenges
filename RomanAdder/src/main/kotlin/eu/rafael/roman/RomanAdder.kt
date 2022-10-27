package eu.rafael.roman



data class RNumber(val i: String, val iv: String, val v: String, val ix: String, val x: String) {
    fun balanceRepeated(digit: Char, carry: Char, repeated: String): Triple<String, String, String> {
        return when(repeated) {
            digit.toString().repeat(4) -> Triple(digit.toString(), carry.toString(), "")
            digit.toString().repeat(5) -> Triple("", carry.toString(), "")
            digit.toString().repeat(6) -> Triple("", carry.toString(), digit.toString())
            else -> Triple("", "", repeated)
        }
    }
    fun balanceAroundPivot(left: StringBuilder, right: StringBuilder) {
        if (left.length > 0 && right.length >= left.length) {
            val len = left.length;
            left.delete(0, len)
            right.delete(0, len)
        }
    }
    fun balance(): RNumber {
        var ni = StringBuilder(i)
        var niv = StringBuilder(iv)
        var nv = StringBuilder(v)
        var nix = StringBuilder(ix)
        var nx = StringBuilder(x)

        /*if (niv.length > 0 && ni.length >= niv.length) {
            val len = niv.length;
            niv.delete(0, len)
            ni.delete(0, len)
        }
        if (nix.length > 0 && ni.length >= nix.length) {
            val len = nix.length;
            nix.delete(0, len)
            ni.delete(0, len)
        }*/
        balanceAroundPivot(niv, ni)
        balanceAroundPivot(nix, ni)

        var t = balanceRepeated('I', 'V', ni.toString());

        niv.append(t.first);
        nv.append(t.second)
        ni.clear().append(t.third)

        /* if (i == "IIII") {
            niv.append("I")
            nv.append("V")
            ni.delete(0, ni.length)
        }
        if (i == "IIIII") {
            nv.append("V")
            ni.delete(0, ni.length)
        }
        if (i == "IIIIII") {
            nv.append("V")
            ni.delete(0, ni.length - 1)
        }*/

        if (niv.contains("II")) {
            ni.append("III")
            nv.delete(0, 1)
            niv.delete(0, 2)
        }
        if (nix.contains("II")) {
            ni.append("III")
            nv.append("V")
            nx.delete(0, 1);
            nix.delete(0, 2)
        }
        if (nv.contains("VV")) {
            nx.append("X")
            nv.delete(0, 2)
        }

        return RNumber(ni.toString(), niv.toString(), nv.toString(), nix.toString(), nx.toString())
    }

    override fun toString(): String {
        var ixstr = if (ix.length == 1) "IX" else ""
        var xstr = x.dropLast(ix.length)
        return xstr + ixstr + iv + v + i;
    }

    companion object {
        fun parse(num: String): RNumber {
            var n = StringBuilder(num.uppercase())
            val i = num.takeLastWhile { it == 'I' }
            var ix = StringBuilder()
            var iv = StringBuilder()
            var v = StringBuilder()
            var x = StringBuilder()
            if (i.isBlank()) {
                if (n.endsWith("IV")) {
                    iv.append("I")
                    v.append("V")
                    n.delete(n.length - 2, n.length)
                } else if (n.endsWith("IX")) {
                    ix.append("I")
                    x.append("X")
                    n.delete(n.length - 2, n.length)
                }
            } else {
                n.delete(n.length - i.length, n.length)
            }

            v.append(n.takeLastWhile { it == 'V' })
            n.delete(n.length - v.length, n.length)
            x.append(n.takeLastWhile { it == 'X' })

            return RNumber(i, iv.toString(), v.toString(), ix.toString(), x.toString()).balance()
        }
    }

    operator fun plus(addend: RNumber): RNumber {
        return RNumber(this.i + addend.i, this.iv + addend.iv, this.v + addend.v,
            this.ix + addend.ix, this.x + addend.x).balance();
    }
}

fun getSeq(seq: String): (String) -> String {
    return {s:String -> if (s.contains(seq)) seq else ""}
}

fun transform(seq: String, target: String): (String) -> String {
    return {if (it == seq) target else ""}
}