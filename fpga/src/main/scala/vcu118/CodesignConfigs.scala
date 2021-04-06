package chipyard.fpga.vcu118

import sys.process._
import chipsalliance.rocketchip.config.Config
import chipyard.config.AbstractConfig
import freechips.rocketchip.devices.tilelink.BootROMLocated
import freechips.rocketchip.diplomacy.DTSTimebase
import freechips.rocketchip.subsystem.{ExtMem, PeripheryBusKey, WithNBigCores}
import gemmini.DefaultGemminiConfig
import sifive.fpgashells.shell.xilinx.{VCU118DDRSize, WithVCU118ShellPMODJTAG}
import testchipip.SerialTLKey

class CodesignVCU118 extends Config(
  new WithFPGAFreq50MHz ++
    new WithVCU118ShellPMODJTAG ++
    new WithUART ++
    new WithDDRMem ++
    new WithUARTIOPassthrough ++
    new WithSPIIOPassthrough ++
    new WithTLIOPassthrough ++
    new WithDefaultPeripherals ++
    new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
    new CodesignModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
    new chipyard.config.WithNoDebug ++ // remove debug module
    new freechips.rocketchip.subsystem.WithoutTLMonitors ++
    new freechips.rocketchip.subsystem.WithNMemoryChannels(1))

class CodesignRocketConfig extends Config(
  new DefaultGemminiConfig ++ // FIXME: use co-design RoCC accel
  new WithNBigCores(1) ++
  new AbstractConfig
)

class CodesignModifications extends Config((site, here, up) => {
  case PeripheryBusKey => up(PeripheryBusKey, site).copy(dtsFrequency = Some(site(FPGAFrequencyKey).toInt*1000000))
  case DTSTimebase => BigInt(1000000)
  case BootROMLocated(x) => up(BootROMLocated(x), site).map { p =>
    // invoke makefile for sdboot
    val freqMHz = site(FPGAFrequencyKey).toInt * 1000000
    val make = s"make -C fpga/src/main/resources/vcu118/bootloop bin"
    require (make.! == 0, "Failed to build bootrom")
    p.copy(hang = 0x10000, contentFileName = s"./fpga/src/main/resources/vcu118/bootloop/build/bootrom.bin")
  }
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(size = site(VCU118DDRSize)))) // set extmem to DDR size
  case SerialTLKey => None // remove serialized tl port
})

class CodesignVCU118Config extends Config(
  new CodesignVCU118 ++
  new CodesignRocketConfig
)