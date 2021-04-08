package chipyard.fpga.vcu118

import chisel3._
import chipyard._
import chipyard.config.AbstractConfig
import chipyard.harness._
import chisel3.experimental.IO
import freechips.rocketchip.subsystem._
import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.jtag.JTAGIO
import gemmini.DefaultGemminiConfig
import sifive.fpgashells.shell._
import sifive.fpgashells.shell.xilinx._

class WithBoardJTAG extends OverrideHarnessBinder({
  (_: HasPeripheryDebug, th: HasHarnessSignalReferences, ports: Seq[Data]) => {
    th match { case vcu118th: VCU118FPGATestHarnessImp => {
      ports.map {
        case sj: JTAGIO =>
          val bj = vcu118th.vcu118Outer.io_jtag_bb.getWrappedValue
          sj.TDI := bj.TDI
          sj.TMS := bj.TMS
          sj.TCK := bj.TCK
          bj.TDO := sj.TDO
      }
    }}
  }
})

class CodesignVCU118 extends Config(
  new WithFPGAFreq50MHz ++
  new WithVCU118ShellPMODSDIO ++
  new WithVCU118ShellPMOD2JTAG ++
  new WithUART ++
  new WithDDRMem ++
  new WithUARTIOPassthrough ++
  new WithSPIIOPassthrough ++
  new WithTLIOPassthrough ++
  new WithBoardJTAG ++
  new WithDefaultPeripherals ++
  new chipyard.config.WithTLBackingMemory ++ // use TL backing memory
  new WithSystemModifications ++ // setup busses, use sdboot bootrom, setup ext. mem. size
  new WithDebugSBA ++
  new WithJtagDTM ++
  new freechips.rocketchip.subsystem.WithoutTLMonitors ++
  new freechips.rocketchip.subsystem.WithNMemoryChannels(1))

class CodesignRocketConfig extends Config(
  new DefaultGemminiConfig ++ // FIXME: use co-design RoCC accel
  new WithNBreakpoints(4) ++
  new WithNBigCores(1) ++
  new AbstractConfig
)

class CodesignVCU118Config extends Config(
  new CodesignVCU118 ++
  new CodesignRocketConfig
)