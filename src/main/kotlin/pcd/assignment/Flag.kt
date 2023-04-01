package pcd.assignment

class Flag {

    private var flag: Boolean  = false

    @Synchronized fun reset() { flag = false }

    @Synchronized fun set() { flag = true }

    @Synchronized fun isSet() = flag
}