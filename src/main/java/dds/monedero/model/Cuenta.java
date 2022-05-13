package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
  }

  public Cuenta(double montoInicial) {
    poner(montoInicial);
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    validarCuanto(cuanto);
    validarMovimientosMaximos();
    agregarMovimiento(LocalDate.now(), cuanto, TipoDeMovimiento.Deposito);
  }

  public void validarMovimientosMaximos(){
    if (getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void sacar(double cuanto) {
    validarCuanto(cuanto);
    validarSaldoDisponible(cuanto);
    validarLimite(cuanto);
    agregarMovimiento(LocalDate.now(), cuanto, TipoDeMovimiento.Extraccion);
  }

  public void validarCuanto(double cuanto){
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void validarSaldoDisponible(double cuanto){
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  public void validarLimite(double cuanto){
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, TipoDeMovimiento esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.fueExtraido(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public double calcularValor;

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return movimientos.stream().mapToDouble(movimiento -> movimiento.getMonto()).sum();
  }

}
