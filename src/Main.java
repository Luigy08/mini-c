import java.io.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.TabableView;

import java.util.ArrayList;
import java.util.function.Function;

public class Main {

	public static ArrayList<ElementoTS> ArregloFunciones = parser.ArregloFunciones; //utilizadp para todo lo que tenga que ver con funciones y llamados
	public static ArrayList<ElementoTS> ArregloSimbolos = parser.ArregloSimbolos; //utilizado para todo lo que tenga que ver con ElementoTS
	public static ArrayList<Cuadruplo> tablaCuadruplos = new ArrayList<>();
	public static ArrayList<String> MIPS = new ArrayList<String>(); //preparacion para el final
	public static ArrayList<String> mensajes = new ArrayList<String>(); //AL de mensajes para codigo final
	public static boolean[] temporales = {false, false, false, false, false, false, false, false, false, false};
	public static boolean[] argumentos = {false, false, false, false};
	public static ArrayList<String> etiquetas = new ArrayList<String>();
	public static ArrayList<String> temporalesIntermedio = new ArrayList<String>();
	public static int contadorTemp = 0;
	public static int contadorEtiq = 0;
	public static boolean primeraVuelta = true;
	public static int siguienteSalto = tablaCuadruplos.size();
	public static String ambitoActual = "%Global";
	public static int offset = 0;
	public static int despPila;
	public static int temporal = 0;
	public static int temporalActual = 0;

	// Manejo de Errores de Tipo en Llamadas de Funcion:
	public static String tipoFuncion = "";
	public static String ErrorFuncion = "";
	public static String FunctionEncontrada = ""; // Incluye el id de la funcion y los parametros.

	static public void main(String argv[]) {

		try {
			parser p = new parser(new Lexer(new FileReader(argv[0])));
			p.parse();
			if (parser.ErroresSintacticos.isEmpty() && Lexer.lexical_errors.isEmpty()) {
        System.out.println(p.ErroresSintacticos.size());
				Nodo root = parser.padre;
        checkTipoAmbito(root);
        TablaCuadruplo.imprimirTablaCuadruplo();
				ImprimirTS1();
				ImprimirTSFunc();
				Graficar(recorrido(root));
        System.out.println("\n");

				codigoFinal();
				System.out.println("AST generado.");
			} else {
				System.out.println("AST no fue generado ya que se presento un error lexico o sintactico.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
  public static void getValueOfId(Nodo root,Nodo padreActual ,String value) {
    for (Nodo node : root.hijos) {

			String valorNodo = node.getValor();
      String etiquetaNodo = node.getEtiqueta();

      if (valorNodo == value) {
        System.out.println("//////////////////");
        System.out.println(valorNodo);
        System.out.println(padreActual.hijos.size());
        for (Nodo elemeNodo : padreActual.hijos) {
        System.out.println(elemeNodo.getValor());

        System.out.println("/--------------/");
        getValueOfId(elemeNodo,null,value); // recursion

        }
      }
      getValueOfId(node,root,value); // recursion

    }
  }

  public static void codigoIntermedioIf (Nodo condicionalActual) {
    System.out.println(condicionalActual.getEtiqueta() + " " + condicionalActual.getValor());
    Nodo operadorCompleto = condicionalActual.hijos.get(0);
    Nodo left = operadorCompleto.hijos.get(0);
    Nodo right = operadorCompleto.hijos.get(1);
    TablaCuadruplo.gen("IF" + operadorCompleto.getValor(),left.getValor(),right.getValor(),"etiq" + Integer.toString(contadorEtiq));
    TablaCuadruplo.gen("GOTO", "_", "_", "_");
    contadorEtiq++;
    if (condicionalActual.hijos.get(1).getEtiqueta().equals("op_rel_completos")) {
    operadorCompleto = condicionalActual.hijos.get(1);
      left = operadorCompleto.hijos.get(0);
      right = operadorCompleto.hijos.get(1);
      TablaCuadruplo.gen("IF" + operadorCompleto.getValor(),left.getValor(),right.getValor(),"etiq" +  Integer.toString(contadorEtiq));
      TablaCuadruplo.gen("GOTO", "_", "_", "_");
      contadorEtiq++;
    } else {
      codigoIntermedioIf(condicionalActual.hijos.get(1));
    }
  }

	public static void checkTipoAmbito(Nodo root) { // sirve para recorrido de intermedio igual
		for (Nodo node : root.hijos) {
      if (node.getError()) {
        System.out.println(node.getMensaje());
      }
			String valorNodo = node.getValor();
			String etiquetaNodo = node.getEtiqueta();

			/*
			 *
			 *
			 *
			 *
			 *
			 *
			 */

			try {
				if (etiquetaNodo == "declaraciones_principales") { // si encuentra una funcion, setear el nuevo ambito
					if (valorNodo == "FUNCTION") {
						ambitoActual = node.getHijos().get(0).getValor();
					}
				}
				if (etiquetaNodo == "cuerpo_main") { // si encontro el cuerpo main, setear el ambito global.
					ambitoActual = "%Global";
				}

			} catch (Exception e) {
			}

			checkTipoAmbito(node); // recursion

			switch (etiquetaNodo) {

			case "declaracionesVARAdentroFunctionProcedure": {

				// son declaraciones internas especiales solo para funciones/procs
				// // agregar id(s) a tabla de simbolos

				ArrayList<Nodo> hijos = node.getHijos();

				Nodo hijo = hijos.get(0);

				Nodo tipovariable = hijos.get(1);
				String tipo = tipovariable.getValor();

				switch (tipo) {
				case "STRING":
				case "INTEGER":
				case "REAL":
				case "CHAR":
				case "BOOLEAN":
				case "RECORD": // si son tipos normales
					int size = hijo.getHijos().size();

					if (hijo.getValor() != null) {
						if (size == 0) { // si es solo unico, solo un ID
							ElementoTS elemento = new ElementoTS();
							elemento.setID(hijo.getValor());
							elemento.setTipo(tipo);
							elemento.setAmbito(ambitoActual);

							boolean seEncontro = buscarExistenciaEnFunciones(elemento);

							// revisar que ese id no sea el mismo que el nombre de la funcion/proc

							boolean esFuncion = RevisarSiEsFuncion(elemento);

							if (esFuncion) {
								// si el elemento es el mismo, tirar error
								System.out.println("Error: Variable duplicada con el nombre de la funcion/proc: "
										+ elemento.getID());
							} else {
								if (!(buscarExistenciaID(elemento)) && !(seEncontro)) {
									ArregloSimbolos.add(elemento);
									// System.out.println("ID agregado: " + elemento.getID() + ", Tipo: "
									// 		+ elemento.getTipo() + ", Ambito: " + ambitoActual);

								} else { // error, ya existe ese simbolo

									if (seEncontro) {
										// si esa nueva variable ya esta en los parametros de la funcion
										System.out.println("Ya existe una variable " + elemento.getID()
												+ " con el tipo de: " + elemento.getTipo()
												+ " declarada en la funcion: " + ambitoActual);
									} else {
										System.out.println(
												"Ya existe una variable " + elemento.getID() + " con el tipo de: "
														+ elemento.getTipo() + ", Ambito: " + ambitoActual);
									}

								}
							}

						}
					} else { // es null cuando son varios
						for (int i = 0; i < size; i++) { // agregar todos los hijos al arreglo

							ElementoTS elemento = new ElementoTS();
							elemento.setID(hijo.getHijos().get(i).getValor());
							elemento.setTipo(tipo);
							elemento.setAmbito(ambitoActual);

							boolean seEncontro = buscarExistenciaEnFunciones(elemento);

							// revisar que el id no sea el mismo que el nombre de la funcion/proc
							boolean esFuncion = RevisarSiEsFuncion(elemento);

							if (esFuncion) {
								System.out.println("Error: Variable duplicada con el nombre de la funcion/proc: "
										+ elemento.getID());
							} else {
								if (!(buscarExistenciaID(elemento)) && !(seEncontro)) {
									ArregloSimbolos.add(elemento);
									// System.out.println("ID agregado: " + elemento.getID() + ", Tipo: "
									// 		+ elemento.getTipo() + ", Ambito: " + elemento.getAmbito());

								} else { // error, ya existe ese simbolo
									if (seEncontro) {
										// si esa nueva variable ya esta en los parametros de la funcion
										System.out.println("Ya existe una variable " + elemento.getID()
												+ " con el tipo de: " + elemento.getTipo()
												+ " declarada en los parametros de la funcion: " + ambitoActual);
									} else {
										System.out.println(
												"Ya existe una variable " + elemento.getID() + " con el tipo de: "
														+ elemento.getTipo() + ", Ambito: " + ambitoActual);
									}

								}
							}

						}
					}
					break;
				default: // si es un tipo Record
					ElementoTS elemento = new ElementoTS();
					elemento.setID(tipo);
					elemento.setTipo("RECORD");
					if (BuscaTipo(elemento)) { // si no existe el Record con ese ID
						// error
						System.out.println("No hay un tipo definido : " + tipo);
					} else { // si ya existe ese Record con el ID
						int size1 = hijo.getHijos().size();
						if (hijo.getValor() != null) {
							if (size1 == 0) { // si es solo unico, solo un ID
								ElementoTS el = new ElementoTS();
								el.setID(hijo.getValor());
								el.setTipo(tipo);
								el.setAmbito(ambitoActual);
								if (BuscaTipo(el)) { // revisar si existe ese record
									// no existe
									// convertir el elemento a tipo record
									el = convertirVariableARecord(el, tipo);
									ArregloSimbolos.add(el);
									// System.out.println("ID agregado: " + el.getID() + ", Tipo: " + el.getTipo()
									// 		+ ", Ambito: " + el.getAmbito());

								} else { // error, ya existe ese simbolo
									System.out.println("Ya existe ese elemento con ese id y tipo.");
								}
							}
						} else { // es null cuando son varios
							for (int i = 0; i < size1; i++) { // agregar todos los hijos al arreglo

								ElementoTS el = new ElementoTS();
								el.setID(hijo.getHijos().get(i).getValor());
								el.setTipo(tipo);
								el.setAmbito(ambitoActual);
								if (BuscaTipo(el)) {

									el = convertirVariableARecord(el, tipo);
									ArregloSimbolos.add(el);
									// System.out.println("ID agregado: " + el.getID() + ", Tipo: " + el.getTipo()
									// 		+ ", Ambito: " + el.getAmbito());

								} else { // error, ya existe ese simbolo
									System.out.println("Ya existe una variable " + el.getID() + " con el tipo de: "
											+ el.getTipo());
								}

							}
						}
					}
				}
				break;
			} // fin case declaracionesVARenfunciones

			case "llamada_procedure_funcion": {

				Nodo id = node.getHijos().get(0); // este es el nodo mas a la izquierda, el mero id de la
													// funcion

				int size = node.getHijos().size();

				if (size == 0) { // significa que no tiene parametros

				} else { // si tiene parametros

					ArrayList<Nodo> parametros = node.getHijos();
					parametros.remove(0); // eliminar el id de la lista de parametros

					ArrayList<ElementoTS> paramNuevos = new ArrayList<ElementoTS>();

					ElementoTS elem = new ElementoTS();
					elem.setID(id.getValor());
					elem.setAmbito(ambitoActual);

					boolean esFuncion = false;

					for (ElementoTS elem2 : ArregloFunciones) {

						if (elem2.getID().equals(elem.getID())) {
							if (elem2.getTipo() == null) {
								// si hay un proc con el mismo nombre esta bien
								esFuncion = false;
								break;
							} else {
								// si encontro una funcion con el mismo id y que tenga parametros
								// no hay que revisar que los parametros sean los mismos
								esFuncion = true;
								break;
							}

						}
					}

					boolean isError = false;

					String parametrosInput = id.getValor() + "(";
					// para impresion de errores

					for (int i = 0; i < parametros.size(); i++) { // convertir nodos a ElementoTS

						Nodo nodo = parametros.get(i);

						ElementoTS param = new ElementoTS();

						String valorNode = nodo.getValor();
						String etiqNodo = nodo.getEtiqueta();

						if (etiqNodo == "Error") {
							// con solo uno que encuentre malo, se tiene que salir
							node.setMensaje(nodo.getMensaje());
							node.setError(true);
							node.setValor("Error");
							isError = true;
							break;
						} else { // si ninguno tira error

							if (etiqNodo == "expresion_matematica") { // si es algo como: N( (1+2), x, y)
								param.setTipo(valorNode);
								param.setID(etiqNodo);
								if (i == parametros.size() - 1) {
									parametrosInput = parametrosInput + param.getID() + ": " + param.getTipo() + ")";
								} else {
									parametrosInput = parametrosInput + param.getID() + ": " + param.getTipo() + ", ";
								}

							} else {
								param.setTipo(etiqNodo);
								param.setID(valorNode);

								if (i == parametros.size() - 1) {
									parametrosInput = parametrosInput + param.getID() + ": " + param.getTipo() + ")";
								} else {
									parametrosInput = parametrosInput + param.getID() + ": " + param.getTipo() + ", ";
								}

							}
							param.setAmbito(ambitoActual);
							paramNuevos.add(param);

						}
					}

					if (isError) {
						// error ya esta seteado en el nodo.
					} else {

						// si todos los parametros estan libres de errores

						elem.setParametros(paramNuevos);

						boolean search = buscarFuncionConParametros(elem, esFuncion);
						// regresa verdadero si encontro la funcion con los parametros mandados

						// impresion de parametros
						// for (ElementoTS param : elem.getParametros()) {
						// System.out.println("Param: " + param.getID() + " , tipo: " +
						// param.getTipo());
						// }

						if (!search) { //si esta todo bien
							node.setValor(tipoFuncion);
							TablaCuadruplo.gen("GOTO", "_", "etiq:" + Integer.toString(contadorEtiq), "_nombreFuncion");
							//TablaCuadruplo.imprimirTablaCuadruplo();
						} else {
							// si encontro un error en el metodo. (revisar metodo)

							System.out.println(" \nError: Se esta llamando una funcion con las siguientes definiciones:"
									+ "\n -> " + parametrosInput + "\n" + "de la cual solo existe esta definicion:"
									+ "\n -> " + FunctionEncontrada + "\n"

							);
							node.setError(true);
							node.setMensaje(ErrorFuncion);

							// System.out.println(ErrorFuncion);

							// for(ElementoTS el : ArregloFunciones){
							// System.out.println("Funcion: " + el.getID());
							// for(ElementoTS param : el.getParametros()){
							// System.out.println("Param: " + param.getID() + " , tipo: " +
							// param.getTipo());
							// }
							// System.out.println();
							// }

							node.setValor("FuncionProc no Encontrada");

						}
					}

				}

				break;
			} // fin case llamada_procedure_funcion
      case "OPSUM": {
        ArrayList<Nodo> hijos = node.getHijos();
        Nodo hijo = hijos.get(0);
        Nodo hijo2 = hijos.get(1);
        if (hijo.getEtiqueta() != "INTEGER" && hijo2.getEtiqueta() != "INTEGER" && hijo.getEtiqueta() != "ID" && hijo2.getEtiqueta() != "ID") {
          TablaCuadruplo.gen(node.getValor(), "t"+ Integer.toString(contadorTemp - 2), "t"+ Integer.toString(contadorTemp - 1), "t" + contadorTemp);
        } else if (hijo.getEtiqueta() != "ID" && hijo.getEtiqueta() != "INTEGER" && (hijo2.getEtiqueta() == "INTEGER" ||hijo2.getEtiqueta() == "ID")) {
          TablaCuadruplo.gen(node.getValor(), "t"+ Integer.toString(contadorTemp - 1), hijo2.getValor(), "t" + contadorTemp);
        } else if ((hijo.getEtiqueta() == "INTEGER" || hijo.getEtiqueta() == "ID") && hijo2.getEtiqueta() != "INTEGER" && hijo2.getEtiqueta() != "ID") {
          TablaCuadruplo.gen(node.getValor(), hijo.getValor(),"t"+ Integer.toString(contadorTemp - 1) ,"t" + contadorTemp);
        } else if ((hijo.getEtiqueta() == "INTEGER" || hijo.getEtiqueta() == "ID") && (hijo2.getEtiqueta() == "INTEGER" || hijo2.getEtiqueta() == "ID")) {
          TablaCuadruplo.gen(node.getValor(), hijo.getValor(),hijo2.getValor() ,"t" + contadorTemp);
        }
        TablaCuadruplo.imprimirTablaCuadruplo();
        temporalesIntermedio.add("t"+contadorTemp);
        contadorTemp++;

        break;
      }
      case "OPMULT": {
        ArrayList<Nodo> hijos = node.getHijos();
        Nodo hijo = hijos.get(0);
        Nodo hijo2 = hijos.get(1);
        if (hijo.getEtiqueta() != "INTEGER" && hijo2.getEtiqueta() != "INTEGER" && hijo.getEtiqueta() != "ID" && hijo2.getEtiqueta() != "ID") {
          TablaCuadruplo.gen(node.getValor(), "t"+ Integer.toString(contadorTemp - 2), "t"+ Integer.toString(contadorTemp - 1), "t" + contadorTemp);
        } else if (hijo.getEtiqueta() != "ID" && hijo.getEtiqueta() != "INTEGER" && (hijo2.getEtiqueta() == "INTEGER" ||hijo2.getEtiqueta() == "ID")) {
          TablaCuadruplo.gen(node.getValor(), "t"+ Integer.toString(contadorTemp - 1), hijo2.getValor(), "t" + contadorTemp);
        } else if ((hijo.getEtiqueta() == "INTEGER" || hijo.getEtiqueta() == "ID") && hijo2.getEtiqueta() != "INTEGER" && hijo2.getEtiqueta() != "ID") {
          TablaCuadruplo.gen(node.getValor(), hijo.getValor(),"t"+ Integer.toString(contadorTemp - 1) ,"t" + contadorTemp);
        } else if ((hijo.getEtiqueta() == "INTEGER" || hijo.getEtiqueta() == "ID") && (hijo2.getEtiqueta() == "INTEGER" || hijo2.getEtiqueta() == "ID")) {
          TablaCuadruplo.gen(node.getValor(), hijo.getValor(),hijo2.getValor() ,"t" + contadorTemp);
        }
        TablaCuadruplo.imprimirTablaCuadruplo();
        temporalesIntermedio.add("t"+contadorTemp);
        contadorTemp++;

        break;
      }
			case "termino": {
				ArrayList<Nodo> hijos = node.getHijos();
				if (hijos.size() == 1) { // si el unico hijo de termino es un factor, subir valor del factor.
					Nodo hijo = hijos.get(0);

					if (hijo.getEtiqueta().equals("expresion_matematica")) {
						// expr_mat tiene el tipo de var en el lado derecho
						node.setValor(hijo.getValor());

						String ErrAcum = hijo.getErrorAcumulado();
						node.setErrorAcumulado(ErrAcum);
					} else {

						// si es cualquier otro tipo de nodo:

						if (hijo.getValor() == "PROCEDURE") {
							node.setValor("PROCEDURE");

						} else if (hijo.getEtiqueta().equals("llamada_procedure_funcion")) {
							node.setValor(hijo.getValor());

							String ErrAcum = node.getValor();
							node.setErrorAcumulado(ErrAcum + "[" + FunctionEncontrada + "]");
						} else {

							if (hijo.getEtiqueta() == "OPBOOL") {
								node.setValor(hijo.getValor());

							} else {
								node.setValor(hijo.getEtiqueta());
							}

							String ErrAcum = node.getValor();
							node.setErrorAcumulado(ErrAcum + "[" + hijo.getValor() + "]");
						}
					}

					if (hijo.getError()) { // quitar valor y subir mensaje
						node.setMensaje(hijo.getMensaje());
						node.setError(true);

						String ErrAcum = hijo.getErrorAcumulado();
						node.setErrorAcumulado(ErrAcum);
						// hijo.setValor("Error de tipos.");
					}
				} else { // si los hijos son un termino y factor

					Nodo hijoTER = hijos.get(0);
					Nodo operando = hijos.get(1);
					Nodo hijoFAC = hijos.get(2);

					// revisar si trae un procedimiento o algo asi

					if (hijoFAC.getValor() == "PROCEDURE" || hijoTER.getValor() == "PROCEDURE") {
						if (hijoFAC.getError() || hijoTER.getError()) { // revisar si alguno de los hijos tiene errores
							node.setError(true);
							if (hijoTER.getError()) {
								node.setMensaje(hijoTER.getMensaje()); // asignar el error de la izquierda
								node.setValor(hijoTER.getValor());
							} else {
								node.setMensaje(hijoFAC.getMensaje()); // asignar el error de la izquierda
								node.setValor("Error de Tipos");
							}

						} else { // si no tiene errores

							if (hijoFAC.getEtiqueta() == "llamada_procedure_funcion"
									&& hijoTER.getValor() == "PROCEDURE") {
								node.setValor(hijoFAC.getValor());
							} else {
								if (hijoFAC.getValor() == hijoTER.getValor()) {
									node.setValor("PROCEDURE");
								} else {
									if (hijoFAC.getValor() == "PROCEDURE") {
										node.setValor(hijoTER.getValor());
									} else if (hijoTER.getValor() == "PROCEDURE") {

										if (hijoFAC.getEtiqueta() == "llamada_procedure_funcion") {
											node.setValor(hijoFAC.getValor());
										} else {
											node.setValor(hijoFAC.getEtiqueta());
										}

									}
								}
							}

						}
					} else {
						// si ninguno es un procedure, revisar si alguno trae un error

						if (hijoFAC.getEtiqueta().equals("expresion_matematica")) {
							if (!(hijoTER.getValor().equals(hijoFAC.getValor()))) {
								node.setError(true);
								if (hijoTER.getError() || hijoFAC.getError()) {
									node.setMensaje(hijoFAC.getMensaje());
									node.setValor("Error de Tipos");
								} else {
									String error = "";

									if (ambitoActual == "%Global") {
										error = "Tipos diferentes en expresion matematica. "
												+ "Se esta operando un tipo " + hijoTER.getValor() + " y un tipo "
												+ hijoFAC.getEtiqueta() + " en el main principal";
									} else {
										error = "Tipos diferentes en expresion matematica. "
												+ "Se esta operando un tipo " + hijoTER.getValor() + " y un tipo "
												+ hijoFAC.getEtiqueta() + " en la funcion: " + ambitoActual;
									}

									node.setMensaje(error);
									node.setValor("Error de Tipos.");

								}
							} else {
								node.setValor("INTEGER");
								if (primeraVuelta) {
									TablaCuadruplo.gen(operando.getValor(), hijoTER.getValor(), hijoFAC.getValor(),
											"t" + contadorTemp);
									TablaCuadruplo.imprimirTablaCuadruplo();
                   temporalesIntermedio.add("t"+contadorTemp);
                    contadorTemp++;
								} else {
									TablaCuadruplo.gen(operando.getValor(), hijoTER.getValor(),
											"t" + Integer.toString(contadorTemp - 1), "t" + contadorTemp);
									TablaCuadruplo.imprimirTablaCuadruplo();
                  temporalesIntermedio.add("t"+contadorTemp);
                  contadorTemp++;
								}
								primeraVuelta = false;
							}

						} else {

							// si no es una expresion matematica:

							if (hijoFAC.getEtiqueta().equals("llamada_procedure_funcion")) {

								if (hijoTER.getError() || hijoFAC.getError()) {

									node.setError(true);

									if (hijoTER.getError() && hijoFAC.getError()) { // si ambos tienen errores
										node.setMensaje(hijoTER.getMensaje());
										System.out.println("Error: " + hijoFAC.getMensaje());

										String ErrAcum = hijoTER.getErrorAcumulado() + " " + hijos.get(1).getValor()
												+ " " + hijoFAC.getErrorAcumulado();
										node.setErrorAcumulado(ErrAcum);

									} else {
										if (hijoTER.getError()) {
											node.setMensaje(hijoTER.getMensaje());
										} else if (hijoFAC.getError()) {
											node.setMensaje(hijoFAC.getMensaje());
										}

										String ErrAcum = hijoTER.getErrorAcumulado() + " " + hijos.get(1).getValor()
												+ " " + hijoFAC.getErrorAcumulado();
										node.setErrorAcumulado(ErrAcum);
									}

									// System.out.println("Mensaje1: " + node.getMensaje());
									node.setValor("Error de Tipos");
								} else { // no hay errroes
									if (hijoTER.getEtiqueta() != "expresion_matematica") {
										if (hijoTER.getValor() == hijoFAC.getValor()) {
											node.setValor(hijoTER.getValor());
										}
									}
								}

							} else {

								boolean isError = false;

								if (hijoTER.getError() || hijoFAC.getError()) { // si ambos tienen errores

									isError = true;
								}

								if (!(hijoTER.getValor().equals(hijoFAC.getEtiqueta())) || isError) {

									node.setError(true);
									if (hijoTER.getError() || hijoFAC.getError()) {

										if (hijoTER.getError() && hijoFAC.getError()) { // si ambos tienen errores
											node.setMensaje(hijoTER.getMensaje());
											System.out.println("Error: " + hijoFAC.getMensaje());

											String ErrAcum = hijoTER.getErrorAcumulado() + " " + hijos.get(1).getValor()
													+ " " + hijoFAC.getErrorAcumulado();
											node.setErrorAcumulado(ErrAcum);

										} else {
											if (hijoTER.getError()) {
												node.setMensaje(hijoTER.getMensaje());

											} else if (hijoFAC.getError()) {
												node.setMensaje(hijoFAC.getMensaje());

											}

											String ErrAcum = hijoTER.getErrorAcumulado() + " " + hijos.get(1).getValor()
													+ " " + hijoFAC.getErrorAcumulado();
											node.setErrorAcumulado(ErrAcum);
										}

										// System.out.println("Mensaje1: " + node.getMensaje());
										node.setValor("Error de Tipos");
									} else {

										String error = "";

										if (ambitoActual == "%Global") {
											error = "Tipos diferentes en expresion matematica. "
													+ "Se esta operando un tipo " + hijoTER.getValor() + " y un tipo "
													+ hijoFAC.getEtiqueta() + " en el main principal";
										} else {
											error = "Tipos diferentes en expresion matematica. "
													+ "Se esta operando un tipo " + hijoTER.getValor() + " y un tipo "
													+ hijoFAC.getEtiqueta() + " en la funcion: " + ambitoActual;
										}

										node.setMensaje(error);
										node.setValor("Error de Tipos.");

										String ErrAcum = hijoTER.getErrorAcumulado() + " " + hijos.get(1).getValor()
												+ " " + hijoFAC.getEtiqueta() + "[" + hijoFAC.getValor() + "]";
										node.setErrorAcumulado(ErrAcum);

									}
								} else {
									node.setValor("INTEGER");
									if (primeraVuelta) {
										TablaCuadruplo.gen(operando.getValor(), hijoTER.getValor(), hijoFAC.getValor(),
												"t" + contadorTemp);
                    TablaCuadruplo.imprimirTablaCuadruplo();
                    temporalesIntermedio.add("t"+contadorTemp);
                    contadorTemp++;
									} else {
										TablaCuadruplo.gen(operando.getValor(), hijoTER.getValor(),
												"t" + Integer.toString(contadorTemp - 1), "t" + contadorTemp);
										TablaCuadruplo.imprimirTablaCuadruplo();
                    temporalesIntermedio.add("t"+contadorTemp);
                    contadorTemp++;
									}
									primeraVuelta = false;
								}
							}

						}
					}

				}

				break;
			} // fin case termino
			case "factor": {

				ArrayList<Nodo> hijos = node.getHijos();

				if (hijos.size() == 1) {
					// puede ser varias cosas:
					/*
					 * un unico identificador num constring constchar otra expresion matematica
					 * entre parentesis
					 */
					String etiquetaHijo = hijos.get(0).getEtiqueta();
					String valorHijo = hijos.get(0).getValor();

					switch (etiquetaHijo) {
					case "ID": {
						// primero buscarlo en el ambito de funciones, despues en ArregloSimbolos

						ElementoTS elem = new ElementoTS();
						elem.setAmbito(ambitoActual);
						elem.setID(valorHijo);

						String search = buscarEnFunciones(elem);

						if (search != "false") {
							// si NO es falso, entonces signfica que si encontro la variable
							// con respecto a su funcion y el ambitoActual.
							node.setEtiqueta(search);
							node.setValor(valorHijo);

							String ErrAcum = search;
							node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");

						} else { // si no la encontro en una funcion que pueda ser que este llamada.

							String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

							// System.out.println("Tipo: " + tipo + ", id; " + valorHijo);

							boolean searchAmbito = BuscaTipoAmbito(elem);

							if (tipo == "Error") { // si no la encontro globalmente, tirar error.

								String error = "";

								if (!searchAmbito) { // si tampoco la encontro en el ambito
									if (ambitoActual == "%Global") {
										error = "No se encontro una variable de ID " + valorHijo
												+ " declarada globalmente.";
									} else {
										error = "No se encontro una variable declarada " + valorHijo
												+ " en la funcion: " + ambitoActual + " ni declarada globalmente.";
									}

									// System.out.println(error);
									node.setMensaje(error);
									node.setEtiqueta("Error");
									node.setError(true);

									String ErrAcum = "TipoNoEncontrado";
									node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");
								} else { // esta cheque porque la encontro en el ambito de la funcion
									String tipoVar = returnTipoAmbito(elem);
									node.setEtiqueta(tipoVar);
									node.setValor(valorHijo);

									String ErrAcum = tipoVar;
									node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");
								}

								// if (ambitoActual != "%Global") {
								// error = "No se encontro una variable declarada " + valorHijo + " en la
								// funcion: "
								// + ambitoActual;
								// } else {
								// error = "No se encontro una variable declarada " + valorHijo
								// + " declarada globalmente.";
								// }

							} else {

								if (searchAmbito) { // si es Verdadero, la encontro en el ambito
									String tipoVar = returnTipoAmbito(elem);
									node.setEtiqueta(tipoVar);
									node.setValor(valorHijo);

									String ErrAcum = tipoVar;
									node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");

								} else {
									// Si solo la encontro globalmente:
									node.setEtiqueta(tipo);
									node.setValor(valorHijo);

									String ErrAcum = tipo;
									node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");

								}

							}

						}

						break;
					} // fin case "ID"
					case "CHAR": {
						node.setEtiqueta("CHAR");
						node.setValor(valorHijo);

						String ErrAcum = "CHAR";
						node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");
						break;
					}
					case "STRING": {
						node.setEtiqueta("STRING");
						node.setValor(valorHijo);

						String ErrAcum = "STRING";
						node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");
						break;
					}
					case "NUM": {
						node.setEtiqueta("INTEGER");
						node.setValor(valorHijo);

						String ErrAcum = "INTEGER";
						node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");

						break;
					}

					case "OPBOOL": {
						node.setEtiqueta("OPBOOL");
						node.setValor(valorHijo);

						String ErrAcum = "OPBOOL";
						node.setErrorAcumulado(ErrAcum + "[" + valorHijo + "]");

						break;
					}

					case "expresion_matematica": {
						Nodo expr = hijos.get(0);
						// subir valores
						// esto es expresion_matematica -> (expresion_matematica)

						String ErrAcum = expr.getErrorAcumulado();
						node.setErrorAcumulado("(" + ErrAcum + ")");
						if (expr.getError()) {
							node.setError(true);
							expr.setValor("Error de tipos.");
						}
						break;
					}
					}

				} else { // cuando es un ID.ID (record)
					// buscar si esa variable record existe

					// String etiquetaHijoIzquierda = hijos.get(0).getEtiqueta();
					String valorHijoIzquierda = hijos.get(0).getValor();

					// String etiquetaHijoDerecha = hijos.get(1).getEtiqueta();
					String valorHijoDerecha = hijos.get(1).getValor();

					ElementoTS elemento = new ElementoTS();
					String tipo = returnTipoRecord(valorHijoIzquierda); // ID = nombre_record

					if (tipo.equals("Error")) {

					} else {
						elemento = returnElementoTS(valorHijoIzquierda, tipo);
					}

					if (BuscaTipoAmbitoActual(elemento)) { // regresa falso si lo encuentra
						// no encontro ese id

						String error = "";

						if (ambitoActual == "%Global") {
							error = "No existe una variable record con ese ID: " + valorHijoIzquierda
									+ " declarada globalmente.";
						} else {
							error = "No existe una variable record con ese ID: " + valorHijoIzquierda
									+ " declarada en la funcion/proc: " + ambitoActual;
						}

						node.setMensaje(error);
						node.setError(true);
						node.setValor("Record no Encontrado");

						String ErrAcum = "VarNotFound" + "[" + valorHijoIzquierda + "]";
						node.setErrorAcumulado(ErrAcum);
						// error
					} else { // si encontro el record

						// ahora revisar si el otro identificador existe declarado en record

						if (!(checkRecordVarConID(elemento, valorHijoDerecha))) { // retorna falso si lo encontro
							String tipo2 = returnTipoVariableRecord(elemento, valorHijoDerecha);
							node.setEtiqueta(tipo2);
							node.setValor(valorHijoIzquierda + "." + valorHijoDerecha);
						} else {
							// no existe ese atributo en el record
							// error
							System.out.println("No existe un atributo: " + valorHijoDerecha
									+ " en un record con ese ID: " + valorHijoIzquierda);
							node.setEtiqueta("Error");
						}

					}
				}
				// System.out.println(node.getEtiqueta() + " - " + node.getValor() + " - " +
				// ambitoActual);
				break;
			} // fin case factor

			case "expresion_matematica": {
				/*
				 * Pueden ser varios casos; 1. Termino 2. OPSUM termino (no utilizado) 3.
				 * expresion_mat OPSUM termino
				 *
				 *
				 */
				ArrayList<Nodo> hijos = node.getHijos();

				switch (hijos.size()) {
				case 1: { // cuando es solo un termino que va a subir. tambien puede subir otra expr

					// revisar si es una funcion o procedimiento de paso

					//

					Nodo hijo = hijos.get(0);

					if (hijo.getEtiqueta() == "expresion_matematica") { // si es algo como expr -> (expr)
						String ErrAcum = hijos.get(0).getErrorAcumulado();
						node.setErrorAcumulado("(" + ErrAcum + ")");
					} else {
						String ErrAcum = hijos.get(0).getErrorAcumulado();
						node.setErrorAcumulado(ErrAcum);
					}

					if (hijo.getError()) { // subir error y mensaje
						node.setError(true);
						hijo.setValor("Error de tipos.");
						node.setValor("Error de tipos."); // setear el error al nodo actual.
						node.setMensaje(hijo.getMensaje());
					} else {
						node.setValor(hijo.getValor());

					}
					break;
				}
				case 2: {

					break;
				}
				case 3: {

					Nodo hijoMAT = hijos.get(0);
					Nodo hijoTER = hijos.get(2);
					Nodo operando = hijos.get(1);

					if (hijoMAT.getValor() == "PROCEDURE" || hijoTER.getValor() == "PROCEDURE") {
						if (hijoMAT.getError() || hijoTER.getError()) { // revisar si alguno de los hijos tiene errores
							node.setError(true);
							if (hijoTER.getError()) {
								node.setMensaje(hijoTER.getMensaje()); // asignar el error de la izquierda
								node.setValor(hijoTER.getValor());
							} else {
								node.setMensaje(hijoMAT.getMensaje()); // asignar el error de la izquierda
								node.setValor("Error de Tipos");
							}

						} else { // si no tiene errores
							if (hijoMAT.getValor() == hijoTER.getValor()) {
								node.setValor("PROCEDURE");
							} else {
								if (hijoMAT.getValor() == "PROCEDURE") {
									node.setValor(hijoTER.getValor());
								} else if (hijoTER.getValor() == "PROCEDURE") {
									node.setValor(hijoMAT.getValor());
								}
							}
						}
					} else {

						boolean isError = false;

						if (hijoMAT.getError() || hijoTER.getError()) { // revisar si alguno de los hijos tiene
							// errores

							isError = true;

						}

						if (!(hijoMAT.getValor() == hijoTER.getValor()) || isError) { // si los valores no son iguales,
																						// hay
							// inconsistencia de tipos
							node.setError(true);
							if (hijoMAT.getError() || hijoTER.getError()) { // revisar si alguno de los hijos tiene
																			// errores

								if (hijoTER.getError() && hijoMAT.getError()) { // si ambos tienen errores
									node.setMensaje(hijoTER.getMensaje());
									System.out.println("Error: " + hijoMAT.getMensaje());

									String ErrAcum = hijoMAT.getErrorAcumulado() + " " + hijos.get(1).getValor() + " "
											+ hijoTER.getErrorAcumulado();
									node.setErrorAcumulado(ErrAcum);
								} else {
									if (hijoTER.getError()) {
										node.setMensaje(hijoTER.getMensaje()); // asignar el error de la izquierda
										node.setValor(hijoTER.getValor());

										String ErrAcum = hijoMAT.getErrorAcumulado() + " " + hijos.get(1).getValor()
												+ " " + hijoTER.getErrorAcumulado();
										node.setErrorAcumulado(ErrAcum);

									} else if (hijoMAT.getError()) {
										node.setMensaje(hijoMAT.getMensaje());
										node.setValor(hijoMAT.getValor());

										String ErrAcum = hijoMAT.getErrorAcumulado() + " " + hijos.get(1).getValor()
												+ " " + hijoTER.getErrorAcumulado();
										node.setErrorAcumulado(ErrAcum);

									}
								}

							} else { // si todo esta bien, asignar error de inconsistencia a nodo padre

								String error = "";

								if (ambitoActual == "%Global") {
									error = "Tipos diferentes en expresion matematica. " + "Se esta operando un tipo "
											+ hijoMAT.getValor() + " y un tipo " + hijoTER.getValor()
											+ " en el main princial";
								} else {
									error = "Tipos diferentes en expresion matematica. " + "Se esta operando un tipo "
											+ hijoMAT.getValor() + " y un tipo " + hijoTER.getValor()
											+ " en la funcion o procedimiento: " + ambitoActual;
								}
								node.setMensaje(error);

								node.setValor("Error de Tipos.");

								String ErrAcum = hijoMAT.getErrorAcumulado() + " " + hijos.get(1).getValor() + " "
										+ hijoTER.getErrorAcumulado();
								node.setErrorAcumulado(ErrAcum);

								// System.out.println(ErrAcum);

							}

						} else { // setear el valor si todo esta bien
							node.setValor(hijoMAT.getValor());
							if (primeraVuelta) { // revisar cuando se tiene que meter el valor o temporal en el cuadruplo
								TablaCuadruplo.gen(operando.getValor(), hijoTER.getValor(), hijoMAT.getValor(),
										"t" + contadorTemp);
									//System.out.println("contador de temp en: " + Integer.toString(contadorTemp));
								TablaCuadruplo.imprimirTablaCuadruplo();
                temporalesIntermedio.add("t"+contadorTemp);
                contadorTemp++;
							} else {
								TablaCuadruplo.gen(operando.getValor(), "t" + Integer.toString(contadorTemp - 1),
										hijoMAT.getValor(), "t" + contadorTemp);
								TablaCuadruplo.imprimirTablaCuadruplo();
                temporalesIntermedio.add("t"+contadorTemp);
                contadorTemp++;
							}
							primeraVuelta = false;
						}
					}

					break;
				}
				default: {

					break;
				}
				}

				// System.out.println(node.getEtiqueta() + " - " + node.getValor() + " - " +
				// ambitoActual);
				break;
			} // fin case expresion mat

			case "cuerpoProposiciones":
      case "asignacionVAR":
			case "proposicion": {
				String valorProp = node.getValor();
        Nodo marcador = new Nodo();
        if (valorProp.equals("=")) {
          ArrayList<Nodo> hijos = node.getHijos();
          Nodo hijo = hijos.get(0);
          Nodo hijo2 = hijos.get(1);
          if (!hijo2.getEtiqueta().equals("INTEGER") && !hijo2.getEtiqueta().equals("CHAR") && !hijo2.getEtiqueta().equals("ID")) {
          System.out.println(hijo2.getEtiqueta() + " | " + hijo.getEtiqueta());
          TablaCuadruplo.gen(node.getValor(), "t"+ Integer.toString((contadorTemp - 1) < 0 ? 0 : (contadorTemp - 1)), "_", hijo.getValor());
          } else{
            TablaCuadruplo.gen(node.getValor(), hijo2.getValor(),"_", hijo.getValor());
          }
          TablaCuadruplo.imprimirTablaCuadruplo();
        } else if(node.getValor().equals("IF")){
          codigoIntermedioIf(node.hijos.get(0).hijos.get(0)); //envio del primer condicional
        } else if(node.getEtiqueta().equals("proposicion") && valorProp.equals("WHILE")){
					ArrayList<Nodo> hijos = node.getHijos();
					Nodo hijo1 = hijos.get(0); //nodo de condicion
					Nodo hijo2 = hijos.get(1); //nodo de prop
					Nodo M1 = new Nodo();
					Nodo M2 = new Nodo();

						ArrayList<Nodo> children = hijo1.getHijos(); //hijos de la expresion
						Nodo expr1 = children.get(0); //expresion a la izq
						Nodo expr2 = children.get(1); //expresion a la der

						String valorexpr1 = expr1.getValor();
						String valorexpr2 = expr2.getValor();

						hijo1.listaVerdadera = Backpatch.crearLista(siguienteSalto);
						hijo1.listaFalsa = Backpatch.crearLista(siguienteSalto + 1);

						TablaCuadruplo.gen(hijo1.getValor(), valorexpr1, valorexpr2, "etiq: " + Integer.toString(contadorEtiq));
						contadorEtiq++;

						TablaCuadruplo.gen("GOTO", "_", "_", "_");
						TablaCuadruplo.imprimirTablaCuadruplo();

						Backpatch.completa(hijo1.getListaVerdadera(), M2.getCuad());
						node.setListaSig(hijo1.getListaFalsa());
						Backpatch.completa(hijo2.getListaSig(), M1.getCuad());

						tablaCuadruplos.add(new Cuadruplo("GOTO", " ", " ", String.valueOf(M1.getCuad())));
						/*M1.setEtiqueta("etiq:" + Integer.toString(contadorEtiq));
						contadorEtiq++;

						M2.setEtiquetaV("etiq:" + Integer.toString(contadorEtiq));
						contadorEtiq++;

						Backpatch.completa(hijo1.listaVerdadera, M2.lineaCuadruplo);
						node.listaSiguiente = hijo1.listaFalsa;
						Backpatch.completa(hijo2.listaSiguiente, M1.lineaCuadruplo);
						TablaCuadruplo.gen("GOTO", "_","_", M2.getEtiquetaV());
						TablaCuadruplo.imprimirTablaCuadruplo();*/


				}else if(node.getEtiqueta().equals("proposicion") && valorProp.equals("FOR")){
					ArrayList<Nodo> hijos = node.getHijos();
					Nodo M1 = new Nodo();
					Nodo M2 = new Nodo();
					Nodo M3 = new Nodo();
					Nodo N = new Nodo();
					//Nodo hijo = hijos.get(0); //agarrando la asignacion principal
					Nodo proposicion = hijos.get(1);
					Nodo incdec = hijos.get(2);
					Nodo S1 = hijos.get(3);

					//proposicion es el hijo condicion y despues procedemos a conseguir los hijos de condicion
					ArrayList<Nodo> childprop = proposicion.getHijos();
					Nodo firstchild = childprop.get(0);
					Nodo secondchild = childprop.get(1);

					//metiendo al cuadruplo la condicion
					TablaCuadruplo.gen(proposicion.getValor(), firstchild.getValor(), secondchild.getValor(), "t" + Integer.toString(contadorTemp));

					//creando intermedio para el nodo N
					N.listaSiguiente = Backpatch.crearLista(siguienteSalto);
					tablaCuadruplos.add(new Cuadruplo("GOTO","_", "_", "_"));

					//genera el intermedio del decremento/incremento
					if(incdec.getValor().equals("i--")){
						TablaCuadruplo.gen("-", "t" + Integer.toString(contadorTemp), "1", "t" + Integer.toString(contadorTemp + 1));
						TablaCuadruplo.gen("=","t" + Integer.toString(contadorTemp), "_", "i");
					}else if(incdec.getValor().equals("i++")){
						TablaCuadruplo.gen("+", "t" + Integer.toString(contadorTemp), "1", "t" + Integer.toString(contadorTemp));
						TablaCuadruplo.gen("=","t" + Integer.toString(contadorTemp-1), "_", "i");
					}

					//generando intermedio del FOR como tal
					Backpatch.completa(S1.getListaSig(), M2.getCuad());
					Backpatch.completa(proposicion.getListaVerdadera(), M3.getCuad());
					node.listaSiguiente = proposicion.getListaFalsa();
					Backpatch.completa(N.getListaSig(), M1.getCuad());
					TablaCuadruplo.gen("GOTO","_", "_", String.valueOf(M2.getCuad()));

					tablaCuadruplos.add(new Cuadruplo("GOTO", "_", "_", String.valueOf(M2.getCuad())));

					/*ArrayList<Nodo> children = hijo.getHijos();
					Nodo expr1 = children.get(0);
					//System.out.println("valor de expr1: " + expr1.getValor());
					Nodo expr2 = children.get(1);
					//System.out.println("valor de expr1: " + expr1.getValor());

					ArrayList<Nodo> childExpr2 = expr2.getHijos();
					Nodo hijo1 = childExpr2.get(0);

					ArrayList<Nodo> childOfChild = hijo1.getHijos();
					Nodo hijo2 = childOfChild.get(0);

					TablaCuadruplo.gen("=", hijo2.getValor(), expr1.getValor(), "I");
					//TablaCuadruplo.imprimirTablaCuadruplo();

					Backpatch.completa(firstchild.listaSiguiente, siguienteSalto);
					//System.out.println("siguientesalto: " + Integer.toString(siguienteSalto));
					TablaCuadruplo.gen("=",expr1.getValor(),"t" + Integer.toString(contadorTemp - 1),"_");
					TablaCuadruplo.imprimirTablaCuadruplo();*/


				}else if(node.getEtiqueta().equals("proposicion") && valorProp.equals("printf")){
          ArrayList<Nodo> hijos = node.getHijos();

          if (hijos.size() == 2) {
            Nodo hijo = hijos.get(0);
            Nodo hijo2 = hijos.get(1);
            String valorexpr1 = hijo.getValor();
            String valorexpr2 = hijo2.getValor();
            // getValueOfId(root,null,valorexpr2);
            TablaCuadruplo.gen("printf",valorexpr1,valorexpr2,"_msg");
            tablaCuadruplos.add(new Cuadruplo("printf", valorexpr1, valorexpr2,"_msg"));
          }
          if (hijos.size() == 1) {
            Nodo hijo = hijos.get(0);
            String valorexpr1 = hijo.getValor();
            System.out.println("/////////////////");
            System.out.println(hijo.getEtiqueta());
            if (hijo.getEtiqueta().equals("CONSTSTRING")) {
              TablaCuadruplo.gen("printf",valorexpr1,"_","_msg");
              tablaCuadruplos.add(new Cuadruplo("printf", valorexpr1,"_","_msg"));
            }else {
              TablaCuadruplo.gen("printf","_",valorexpr1,"_msg");
            tablaCuadruplos.add(new Cuadruplo("printf", "_",valorexpr1,"_msg"));
            }

          }
          TablaCuadruplo.imprimirTablaCuadruplo();



					//TablaCuadruplo.imprimirTablaCuadruplo();

				}else if(node.getEtiqueta().equals("proposicion") && valorProp.equals("scanf")){
					ArrayList<Nodo> hijos = node.getHijos();
					Nodo hijo = hijos.get(0);

					String valorexpr1 = hijo.getValor();

					TablaCuadruplo.gen(valorProp,valorexpr1,"_", "_msg");
					//TablaCuadruplo.imprimirTablaCuadruplo();
				}
        TablaCuadruplo.imprimirTablaCuadruplo();

			} //fin proposicion de intermedio



				boolean esCuerpoProcedure = false;

				if (node.getEtiqueta() == "cuerpoProposiciones") {
					// es un tipo de nodo proposicion que aparece cuando hay solo una proposicion
					// con un record.
					esCuerpoProcedure = true;
				}

				if (node.getValor().equals(":=") || esCuerpoProcedure) { // si el nodo proposicion trae una asignacion.
					ArrayList<Nodo> hijos = node.getHijos();

					if (hijos.size() == 2) { // cuando es solo una asignacion que no tiene records
						// x := algo
						Nodo id = hijos.get(0);
						Nodo expr = hijos.get(1);

						String valorHijo = id.getValor();

						// comprobacion de tipos
						// revisar que T1 sea el mismo tipo de expresion_matematica

						// primero hay que buscarlo en las declaraciones de funcion

						ElementoTS elem = new ElementoTS();
						elem.setAmbito(ambitoActual);
						elem.setID(id.getValor());

						String searchInicial = buscarEnFunciones(elem); // busca en los parametros de la funcion

						String tipo = returnTipoGlobal(valorHijo); // busqueda global

						boolean esRetorno = RevisarSiEsFuncion(elem);

						if (searchInicial != "false") {
							// si NO es falso, entonces signfica que si encontro la variable
							// con respecto a su funcion y el ambitoActual.

							// node.setEtiqueta(searchInicial);
							// node.setValor(id.getValor());

							elem.setTipo(searchInicial);

							if (expr.getError()) { // si expr_mat es un error
								System.out.println("Error: " + expr.getMensaje());
								expr.setValor("Error de Tipos.");

								String ErrAcum = expr.getErrorAcumulado();
								ErrAcum = elem.getTipo() + "[" + elem.getID() + "]" + " := " + ErrAcum;
								node.setErrorAcumulado(ErrAcum);

								System.out.println("Log de Error:\n" + ErrAcum);
							} else { // si todo esta bien, revisar si son iguales.
								if (tipo.equals(expr.getValor())
										|| (tipo.equals("INTEGER") && expr.getValor().equals("NUM"))) {
									// cheque

								}
								if ((tipo.equals("BOOLEAN") && expr.getValor().equals("true"))) {
									// cheque

								}
								if ((tipo.equals("BOOLEAN") && expr.getValor().equals("false"))) {
									// cheque

								} else {
									System.out.println("Error: " + "El tipo de " + id.getValor()
											+ " es diferente que el valor asignado. Se esperaba " + tipo);

								}
							}
						} else {

							// si no esta en los parametros, revisar globales y locales

							boolean searchAmbito = BuscaTipoAmbito(elem); // local

							if (tipo.equals("Error")) {

								String error = "";

								if (!searchAmbito) { // si tampoco la encontro en el ambito

									if (esRetorno) { // si el lado izquierdo de la asignacion es un retorno
										// cheq
										// revisar si el tipo de ambos lados es el mismo

										String tipoFunc = returnTipoFuncion(elem);

										// Ahora revisar si tiene errores.
										if (expr.getError()) { // si expr_mat es un error
											System.out.println("Error: " + expr.getMensaje());
											expr.setValor("Error de Tipos.");
										} else { // si todo esta bien, revisar si son iguales.
											if (tipoFunc.equals(expr.getValor())
													|| (tipoFunc.equals("INTEGER") && expr.getValor().equals("NUM"))) {
												// cheque

											} else {

												if (expr.getValor() == "PROCEDURE") {
													// cheque
												}
												if ((tipoFunc.equals("BOOLEAN") && expr.getValor().equals("true"))) {
													// cheque

												}
												if ((tipoFunc.equals("BOOLEAN") && expr.getValor().equals("false"))) {
													// cheque

												} else {
													System.out.println("Error: " + "El tipo de " + id.getValor()
															+ " es diferente que el valor asignado. Se esperaba "
															+ tipoFunc);
												}

											}
										}

									} else {
										if (ambitoActual == "%Global") {
											error = "No se encontro una variable de ID " + valorHijo
													+ " declarada globalmente.";
										} else {
											error = "No se encontro una variable declarada " + valorHijo
													+ " en la funcion: " + ambitoActual + " ni declarada globalmente.";
										}
										node.setMensaje(error);
										node.setEtiqueta("Error");
										System.out.println(error);
									}

								} else { // esta cheque porque la encontro en el ambito de la funcion

									// Ahora revisar si tiene errores.

									if (searchAmbito) { // si la encontro en el ambito cheque.

										String typ = returnTipoAmbito(elem);

										if (expr.getError()) { // si expr_mat es un error
											System.out.println("Error: " + expr.getMensaje());
											expr.setValor("Error de Tipos.");

										} else { // si todo esta bien, revisar si son iguales.
											if (typ.equals(expr.getValor()) || (searchInicial.equals("INTEGER")
													&& expr.getValor().equals("NUM"))) {
												// cheque

											} else if ((typ.equals("BOOLEAN") && expr.getValor().equals("true"))) {
												// cheque

											} else if ((typ.equals("BOOLEAN") && expr.getValor().equals("false"))) {
												// cheque

											} else {
												if (expr.getValor() == "PROCEDURE") {
													// cheque
												} else {
													System.out.println("Error: " + "El tipo de " + id.getValor()
															+ " es diferente que el valor asignado. Se esperaba "
															+ tipo);
												}
											}
										}
									} else {// si lo encontro pero globalmente.
										// ahora a revisar si la parte derecha tiene errroes
										if (expr.getError()) { // si expr_mat es un error
											System.out.println("Error: " + expr.getMensaje());
											expr.setValor("Error de Tipos.");

											String ErrAcum = expr.getErrorAcumulado();
											ErrAcum = elem.getTipo() + " := " + ErrAcum;
											node.setErrorAcumulado(ErrAcum);

											System.out.println("Log de Error:\n" + ErrAcum);

										} else { // si todo esta bien, revisar si son iguales.
											if (tipo.equals(expr.getValor())
													|| (tipo.equals("INTEGER") && expr.getValor().equals("NUM"))) {
												// cheque

											} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("true"))) {
												// cheque

											} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("false"))) {
												// cheque

											} else {
												if (expr.getValor() == "PROCEDURE") {
													// cheque
												} else {
													System.out.println("Error: " + "El tipo de " + id.getValor()
															+ " es diferente que el valor asignado. Se esperaba "
															+ tipo);
												}
											}
										}
									}

								}

							} else {

								elem.setTipo(tipo);

								if (searchAmbito) { // si la encontro en el ambito cheque.

									if (expr.getError()) { // si expr_mat es un error
										System.out.println("Error: " + expr.getMensaje());
										expr.setValor("Error de Tipos.");

										String ErrAcum = expr.getErrorAcumulado();
										ErrAcum = elem.getTipo() + "[" + elem.getID() + "]" + " := " + ErrAcum;
										node.setErrorAcumulado(ErrAcum);

										System.out.println("Log de Error:\n" + ErrAcum);
									} else { // si todo esta bien, revisar si son iguales.
										if (tipo.equals(expr.getValor())
												|| (tipo.equals("INTEGER") && expr.getValor().equals("NUM"))) {
											// cheque

										} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("true"))) {
											// cheque

										} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("false"))) {
											// cheque

										} else {
											if (expr.getValor() == "PROCEDURE") {
												// cheque
											} else {
												System.out.println("Error: " + "El tipo de " + id.getValor()
														+ " es diferente que el valor asignado. Se esperaba " + tipo);
											}
										}
									}
								} else {// si lo encontro pero globalmente.
									// ahora a revisar si la parte derecha tiene errroes
									if (expr.getError()) { // si expr_mat es un error
										System.out.println("Error: " + expr.getMensaje());
										expr.setValor("Error de Tipos.");

										String ErrAcum = expr.getErrorAcumulado();
										ErrAcum = elem.getTipo() + " := " + ErrAcum;
										node.setErrorAcumulado(ErrAcum);

										System.out.println("Log de Error:\n" + ErrAcum);

									} else { // si todo esta bien, revisar si son iguales.
										if (tipo.equals(expr.getValor())
												|| (tipo.equals("INTEGER") && expr.getValor().equals("NUM"))) {
											// cheque

										} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("true"))) {
											// cheque

										} else if ((tipo.equals("BOOLEAN") && expr.getValor().equals("false"))) {
											// cheque

										} else {
											if (expr.getValor() == "PROCEDURE") {
												// cheque
											} else {
												System.out.println("Error: " + "El tipo de " + id.getValor()
														+ " es diferente que el valor asignado. Se esperaba " + tipo);
											}
										}
									}
								}

							}
						}

					} else { // si se usa record en el lado izquierdo
						// x.y := algo
						// son tres hijos

						// ahora a revisar si ese id.id realmente existe

						ElementoTS elemento = new ElementoTS();

						Nodo id1 = hijos.get(0);
						Nodo id2 = hijos.get(1);

						Nodo expr = hijos.get(2);

						String tipo = returnTipoRecord(id1.getValor());

						String tipoAtributo = ""; // es el tipo del atributo id.x (tipo de x)

						if (tipo.equals("Error")) {

						} else {
							elemento = returnElementoTS(id1.getValor(), tipo);

						}

						if (BuscaTipoAmbitoActual(elemento)) { // regresa falso si lo encuentra
							// no encontro ese id
							System.out.println("No existe una variable record con ese ID: " + id1.getValor());
							// error
						} else { // si encontro el record

							// ahora revisar si el otro identificador existe declarado en record

							if (!(checkRecordVarConID(elemento, id2.getValor()))) { // retorna falso si lo encontro
								tipoAtributo = returnTipoVariableRecord(elemento, id2.getValor());
							} else {
								// no existe ese atributo en el record
								// error
								System.out.println("No existe un atributo: " + id2.getValor()
										+ " en un record con ese ID: " + id1.getValor());
								node.setEtiqueta("Error");
								tipoAtributo = "Error";
							}

						}

						// comprobacion de otros tipos

						if (expr.getError()) { // si expr_mat es un error
							System.out.println("Error: " + expr.getMensaje());
							expr.setValor("Error de Tipos.");
						} else { // si todo esta bien, revisar si son iguales.
							if (tipoAtributo.equals(expr.getValor())
									|| (tipoAtributo.equals("INTEGER") && expr.getValor().equals("NUM"))) {
								// cheque
							} else {

								if (expr.getValor() == "PROCEDURE") {
									// cheque
								} else {
									System.out.println("Error: " + "El tipo de " + id2.getValor()
											+ " es diferente que el valor asignado. Se esperaba " + tipoAtributo
											+ " y se encontro un " + expr.getValor());
								}
							}
						}

					}

				} else if (node.getValor() == "IF") {

					ArrayList<Nodo> hijos = node.getHijos();

					Nodo hijo = hijos.get(0);

					if (hijo.getError()) {
						System.out.println("Error: " + hijo.getMensaje());
					}

				} else if (node.getValor().equals("write")) {
					// son dos casos, puede haber 0,1,2 params en el write

					ArrayList<Nodo> hijos = node.getHijos();
					int size = hijos.size();

					if (size == 0) {
						// si es algo como write();
						// cheque

					} else if (size == 1) {
						// pueden ser dos casos: write('') o write('algoaqui')
						// verificar ambos

						String valorHijo = hijos.get(0).getValor();

						if (valorHijo.equals("")) {
							// cheque
						} else {
							// Asegurarse que el tipo del parametro sea un string entonces

							if (hijos.get(0).getEtiqueta().equals("CONSTSTRING")) {
								// cheque
							} else {
								// revisar el tipo del id que viene

								ElementoTS elem = new ElementoTS();
								elem.setAmbito(ambitoActual);
								elem.setID(valorHijo);

								String search = buscarEnFunciones(elem);

								if (search != "false") {
									// si NO es falso, entonces signfica que si encontro la variable
									// con respecto a su funcion y el ambitoActual.

									// ahora revisar si es un string

									if (search != "STRING") {
										System.out.println("Error: " + "Primer parametro de un write"
												+ " tiene que ser de tipo STRING");
										System.out.println("Se encontro: " + "write(" + valorHijo + ")");
									} else {
										// cheque
									}

								} else { // si no la encontro en una funcion que pueda ser que este llamada.

									String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

									// System.out.println("Tipo: " + tipo + ", id; " + valorHijo);

									boolean searchAmbito = BuscaTipoAmbito(elem);

									if (tipo == "Error") { // si no la encontro globalmente, tirar error.

										String error = "";

										if (!searchAmbito) { // si tampoco la encontro en el ambito
											if (ambitoActual == "%Global") {
												error = "No se encontro una variable de ID " + valorHijo
														+ " declarada globalmente.";
											} else {
												error = "No se encontro una variable declarada " + valorHijo
														+ " en la funcion: " + ambitoActual
														+ " ni declarada globalmente.";
											}

											System.out.println("Error: " + error);

										} else { // esta cheque porque la encontro en el ambito de la funcion
											String tipoVar = returnTipoAmbito(elem);

											// revisar si es String
											if (tipoVar != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipoVar + "["
														+ valorHijo + "]" + ")");
											} else {
												// cheque
											}
										}

									} else {

										if (searchAmbito) { // si es Verdadero, la encontro en el ambito
											String tipoVar = returnTipoAmbito(elem);

											// revisar si es String
											if (tipoVar != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipoVar + "["
														+ valorHijo + "]" + ")");
											} else {
												// cheque
											}

										} else {
											// Si solo la encontro globalmente:

											// revisar si es String
											if (tipo != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipo + "[" + valorHijo
														+ "]" + ")");
											} else {
												// cheque
											}
										}

									}

								}
							}

						}

					} else if (size == 2 || size == 3) {

						boolean isRecord = false;
						String tipoAtributoRecord = "";

						if (size == 3) {
							isRecord = true;

							ElementoTS elemento = new ElementoTS();

							Nodo id1 = hijos.get(1);
							Nodo id2 = hijos.get(2);

							String tipo = returnTipoRecord(id1.getValor());

							if (tipo.equals("Error")) {
								System.out.println("No se encontro ese record de nombre: " + id1.getValor()
										+ " declarado globalmente.");
							} else {
								elemento = returnElementoTS(id1.getValor(), tipo);

							}

							if (BuscaTipoAmbitoActual(elemento)) { // regresa falso si lo encuentra
								// no encontro ese id
								System.out.println("No existe una variable record con ese ID: " + id1.getValor());
								tipoAtributoRecord = "Error";
								// error
							} else { // si encontro el record

								// ahora revisar si el otro identificador existe declarado en record

								if (!(checkRecordVarConID(elemento, id2.getValor()))) { // retorna falso si lo encontro
									tipoAtributoRecord = returnTipoVariableRecord(elemento, id2.getValor());

								} else {
									// no existe ese atributo en el record
									// error
									System.out.println("No existe un atributo: " + id2.getValor()
											+ " en un record con ese ID: " + id1.getValor());
									tipoAtributoRecord = "Error";
								}

							}
						}

						// primero hay que revisar que el primer hijo es un string

						boolean isOk = false;
						ElementoTS elem = new ElementoTS();

						if (hijos.get(0).getEtiqueta().equals("CONSTSTRING")) {
							isOk = true;
							elem.setTipo("STRING");
						}
						if ((hijos.get(0).getEtiqueta().equals("CONSTSTRING"))
								&& (hijos.get(1).getEtiqueta().equals("CONSTSTRING")
										|| hijos.get(1).getEtiqueta().equals("NUM")
										|| hijos.get(1).getEtiqueta().equals("CONSTCHAR"))) {
							// cheque, el primero y el segundo tienen tipos correctos.
						} else {
							// revisar el tipo del id que viene (lado izquierdo)

							if (!isOk) {
								String valorHijo = hijos.get(0).getValor();

								elem.setAmbito(ambitoActual);
								elem.setID(valorHijo);

								String search = buscarEnFunciones(elem);

								if (search != "false") {
									// si NO es falso, entonces signfica que si encontro la variable
									// con respecto a su funcion y el ambitoActual.

									// ahora revisar si es un string

									if (search != "STRING") {
										System.out.println("Error: " + "Primer parametro de un write"
												+ " tiene que ser de tipo STRING");

									} else {
										// cheque
										isOk = true;
										elem.setTipo("STRING");
									}

								} else { // si no la encontro en una funcion que pueda ser que este llamada.

									String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

									// System.out.println("Tipo: " + tipo + ", id; " + valorHijo);

									boolean searchAmbito = BuscaTipoAmbito(elem);

									if (tipo == "Error") { // si no la encontro globalmente, tirar error.

										String error = "";

										if (!searchAmbito) { // si tampoco la encontro en el ambito
											if (ambitoActual == "%Global") {
												error = "No se encontro una variable de ID " + valorHijo
														+ " declarada globalmente.";
											} else {
												error = "No se encontro una variable declarada " + valorHijo
														+ " en la funcion: " + ambitoActual
														+ " ni declarada globalmente.";
											}

											System.out.println("Error: " + error);

										} else { // esta cheque porque la encontro en el ambito de la funcion
											String tipoVar = returnTipoAmbito(elem);

											// revisar si es String
											if (tipoVar != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipoVar + "["
														+ valorHijo + "]" + ")" + "\n");
											} else {
												// cheque
												elem.setTipo(tipoVar);
												isOk = true;
											}
										}

									} else {

										if (searchAmbito) { // si es Verdadero, la encontro en el ambito
											String tipoVar = returnTipoAmbito(elem);

											// revisar si es String

											if (tipoVar != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipoVar + "["
														+ valorHijo + "]" + ")" + "\n");
											} else {
												// cheque
												elem.setTipo(tipoVar);
												isOk = true;
											}

										} else {
											// Si solo la encontro globalmente:

											// revisar si es String
											if (tipo != "STRING") {
												System.out.println("Error: " + "Primer parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + tipo + "[" + valorHijo
														+ "]" + ")" + "\n");
											} else {
												// cheque
												elem.setTipo(tipo);
												isOk = true;
											}
										}

									}

								} // fin busqueda
							}

							if (isOk) {

								// por si viene un NUM/CHAR/STRING de un solo en el lado derecho:

								if ((hijos.get(1).getEtiqueta().equals("CONSTSTRING")
										|| hijos.get(0).getEtiqueta().equals("INTEGER")
										|| hijos.get(0).getEtiqueta().equals("CHAR"))) {
									// cheque, el primero y el segundo tienen tipos correctos.
								} else {

									// si no, pues ahora hay que revisar que el segundo hijo solo sea
									// NUM/STRING/CHAR

									String valorHijoDer = hijos.get(1).getValor();
									String valorHijo = hijos.get(0).getValor();

									if (isRecord) {
										if (tipoAtributoRecord != "Error") {
											// si el record trae un tipo
											if (tipoAtributoRecord == "STRING" || tipoAtributoRecord == "INTEGER"
													|| tipoAtributoRecord == "CHAR") {
												// cheque

											} else {

												String valorRec = hijos.get(2).getValor();

												System.out.println("Error: " + "Segundo parametro de un write"
														+ " tiene que ser de tipo STRING, CHAR, o INTEGER");
												System.out.println("Se encontro: " + "write(" + elem.getTipo() + "["
														+ valorHijo + "], " + tipoAtributoRecord + "[" + valorHijoDer
														+ "." + valorRec + "]" + ")");
											}
										} else {
											// ya esta el error
										}
									} else {
										ElementoTS elem2 = new ElementoTS();
										elem2.setAmbito(ambitoActual);
										elem2.setID(valorHijoDer);

										String search2 = buscarEnFunciones(elem2);

										if (search2 != "false") {
											// si NO es falso, entonces signfica que si encontro la variable
											// con respecto a su funcion y el ambitoActual.

											// ahora revisar si es un string

											if (search2 == "STRING" || search2 == "INTEGER" || search2 == "CHAR") {
												// cheque

											} else {
												System.out.println("Error: " + "Segundo parametro de un write"
														+ " tiene que ser de tipo STRING");
												System.out.println("Se encontro: " + "write(" + elem.getTipo() + "["
														+ valorHijo + "], " + search2 + "[" + valorHijoDer + "]" + ")");
											}

										} else { // si no la encontro en una funcion que pueda ser que este llamada.

											String tipo2 = returnTipoGlobal(valorHijoDer); // revisar en variables
																							// globales
											boolean searchAmbito2 = BuscaTipoAmbito(elem2); // revisar con respecto
																							// a
																							// ambito

											if (tipo2 == "Error") { // si no la encontro globalmente, tirar error.
												String error = "";
												if (!searchAmbito2) { // si tampoco la encontro en el ambito
													if (ambitoActual == "%Global") {
														error = "No se encontro una variable de ID " + valorHijoDer
																+ " declarada globalmente.";
													} else {
														error = "No se encontro una variable declarada " + valorHijoDer
																+ " en la funcion: " + ambitoActual
																+ " ni declarada globalmente.";
													}
													System.out.println("Error: " + error);
												} else { // esta cheque porque la encontro en el ambito de la
															// funcion
													String tipoVar2 = returnTipoAmbito(elem2);
													// revisar si es String
													if (tipoVar2 == "STRING" || tipoVar2 == "INTEGER"
															|| tipoVar2 == "CHAR") {
														// cheque

													} else {
														System.out.println("Error: " + "Segundo parametro de un write"
																+ " tiene que ser de tipo STRING");
														System.out.println("Se encontro: " + "write(" + elem.getTipo()
																+ "[" + valorHijo + "], " + tipoVar2 + "["
																+ valorHijoDer + "]" + ")");
													}
												}
											} else {
												if (searchAmbito2) { // si es Verdadero, la encontro en el ambito
													String tipoVar2 = returnTipoAmbito(elem2);
													// revisar si es String
													if (tipoVar2 == "STRING" || tipoVar2 == "INTEGER"
															|| tipoVar2 == "CHAR") {
														// cheque

													} else {
														System.out.println("Error: " + "Segundo parametro de un write"
																+ " tiene que ser de tipo STRING");
														System.out.println("Se encontro: " + "write(" + elem.getTipo()
																+ "[" + valorHijo + "], " + tipoVar2 + "["
																+ valorHijoDer + "]" + ")");
													}
												} else {
													// Si solo la encontro globalmente:
													// revisar si es String
													if (tipo2 == "STRING" || tipo2 == "INTEGER" || tipo2 == "CHAR") {
														// cheque

													} else {
														System.out.println("Error: " + "Segundo parametro de un write"
																+ " tiene que ser de tipo STRING");
														System.out.println("Se encontro: " + "write(" + elem.getTipo()
																+ "[" + valorHijo + "], " + tipo2 + "[" + valorHijoDer
																+ "]" + ")");
													}
												}
											}
										} // fin busqueda del lado derecho
									} // fin else isRecord

								} // fin else de comparacion de tipos

							} else {
								// fin isOk, ya esta el error puesto
							}
						}

					}
				} else if (node.getValor().equals("read")) {

					ArrayList<Nodo> hijos = node.getHijos();

					int size = hijos.size();

					// solo puede ser INTEGER O CHAR

					if (size == 1) {
						Nodo hijo = node.getHijos().get(0);

						if (hijo.getEtiqueta().equals("NUM") || hijo.getEtiqueta().equals("CONSTCHAR")
								|| hijo.getEtiqueta().equals("BOOLEAN") || hijo.getEtiqueta().equals("CONSTSTRING")
								|| hijo.getEtiqueta().equals("REAL") || hijo.getEtiqueta().equals("RECORD")) {
							// error
							System.out.println(
									"Error: No se encontro una variable de adentro del read. Tiene que ser tipo INTEGER o CHAR");
							System.out.println(
									"Se encontro: " + "read(" + hijo.getEtiqueta() + "[" + hijo.getValor() + "])");
						} else {
							// revisar tipo

							String valorHijo = hijo.getValor();

							ElementoTS elem = new ElementoTS();
							elem.setAmbito(ambitoActual);
							elem.setID(valorHijo);

							String search = buscarEnFunciones(elem);

							if (search != "false") {
								// si NO es falso, entonces signfica que si encontro la variable
								// con respecto a su funcion y el ambitoActual.

								// ahora revisar si es un string

								if (search != "STRING") {
									System.out.println(
											"Error: " + "Parametro de read" + " tiene que ser de tipo INTEGER o CHAR");
									System.out.println("Se encontro: " + "read(" + valorHijo + ")" + "\n");
								} else {
									// cheque
								}

							} else { // si no la encontro en una funcion que pueda ser que este llamada.

								String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

								// System.out.println("Tipo: " + tipo + ", id; " + valorHijo);

								boolean searchAmbito = BuscaTipoAmbito(elem);

								if (tipo == "Error") { // si no la encontro globalmente, tirar error.

									String error = "";

									if (!searchAmbito) { // si tampoco la encontro en el ambito
										if (ambitoActual == "%Global") {
											error = "No se encontro una variable de ID " + valorHijo
													+ " declarada globalmente.";
										} else {
											error = "No se encontro una variable declarada " + valorHijo
													+ " en la funcion: " + ambitoActual + " ni declarada globalmente.";
										}

										System.out.println("Error: " + error);

									} else { // esta cheque porque la encontro en el ambito de la funcion
										String tipoVar = returnTipoAmbito(elem);

										// revisar si es String
										if (tipoVar == "INTEGER" || tipoVar == "CHAR") {
											// cheque
										} else {

											System.out.println("Error: " + "Parametro de read"
													+ " tiene que ser de tipo INTEGER o CHAR");
											System.out.println("Se encontro: " + "read(" + tipoVar + "[" + valorHijo
													+ "]" + ")" + "\n");
										}
									}

								} else {

									if (searchAmbito) { // si es Verdadero, la encontro en el ambito
										String tipoVar = returnTipoAmbito(elem);

										// revisar si es String
										if (tipoVar == "INTEGER" || tipoVar == "CHAR") {
											// cheque
										} else {
											System.out.println("Error: " + "Parametro de read"
													+ " tiene que ser de tipo INTEGER o CHAR");
											System.out.println("Se encontro: " + "read(" + tipoVar + "[" + valorHijo
													+ "]" + ")" + "\n");
										}

									} else {
										// Si solo la encontro globalmente:

										// revisar si es String
										if (tipo == "INTEGER" || tipo == "CHAR") {
											// cheque
										} else {
											System.out.println("Error: " + "Parametro de read"
													+ " tiene que ser de tipo INTEGER o CHAR");
											System.out.println("Se encontro: " + "read(" + tipo + "[" + valorHijo + "]"
													+ ")" + "\n");
										}
									}

								}

							}

						}

					} else { // viene un record

						String tipoAtributoRecord = "";

						ElementoTS elemento = new ElementoTS();

						Nodo id1 = hijos.get(0);
						Nodo id2 = hijos.get(1);

						String tipo = returnTipoRecord(id1.getValor());

						if (tipo.equals("Error")) {
							System.out.println("No se encontro ese record de nombre: " + id1.getValor()
									+ " declarado globalmente.");
						} else {
							elemento = returnElementoTS(id1.getValor(), tipo);

						}

						if (BuscaTipoAmbitoActual(elemento)) { // regresa falso si lo encuentra
							// no encontro ese id
							System.out.println("No existe una variable record con ese ID: " + id1.getValor());
							tipoAtributoRecord = "Error";
							// error
						} else { // si encontro el record

							// ahora revisar si el otro identificador existe declarado en record

							if (!(checkRecordVarConID(elemento, id2.getValor()))) { // retorna falso si lo encontro
								tipoAtributoRecord = returnTipoVariableRecord(elemento, id2.getValor());

							} else {
								// no existe ese atributo en el record
								// error
								System.out.println("No existe un atributo: " + id2.getValor()
										+ " en un record con ese ID: " + id1.getValor());
								tipoAtributoRecord = "Error";
							}

						}

						if (tipoAtributoRecord != "Error") {
							if (tipoAtributoRecord == "INTEGER" || tipoAtributoRecord == "CHAR") {
								// cheque
							} else {

								System.out.println(
										"Error: " + "Parametro de read" + " debe que ser de tipo INTEGER o CHAR");
								System.out.println("Se encontro: " + "read(" + tipoAtributoRecord + "["
										+ hijos.get(0).getValor() + "." + hijos.get(1).getValor() + "]" + ")");
								;
							}
						} else {
							//
						}
					}

				} else {
					// nada
				}

				break;
			// fin proposicion

			case "expresion": {

				ArrayList<Nodo> hijos = node.getHijos();

				if (hijos.size() != 1) {
					Nodo hijoIzq = hijos.get(0);
					Nodo oprel = hijos.get(1);
					Nodo hijoDer = hijos.get(2);

					if ((hijoIzq.getEtiqueta() == hijoDer.getEtiqueta()) && hijoIzq.getEtiqueta() == "expresion") {

						if (hijoIzq.getError() || hijoDer.getError()) {
							node.setError(true);
							if (hijoIzq.getError() && hijoDer.getError()) {

								node.setMensaje(hijoDer.getMensaje());
								System.out.println("Error: " + hijoIzq.getMensaje());

							} else {
								if (hijoIzq.getError()) {

									node.setValor("Error");
									node.setMensaje(hijoIzq.getMensaje());

								} else if (hijoDer.getError()) {
									node.setValor("Error");
									node.setMensaje(hijoDer.getMensaje());
								}
							}
						} else {
							String val = hijoIzq.getValor() + " " + oprel.getValor() + " " + hijoDer.getValor();

							node.setValor(val);

						}

					} else {
						boolean isOk = false;

						String tipoFinal = "";
						String tipoDer = "";

						if (hijoIzq.getEtiqueta() == "ID") {

							String valorHijo = hijoIzq.getValor();

							ElementoTS elem = new ElementoTS();
							elem.setAmbito(ambitoActual);
							elem.setID(valorHijo);

							String search = buscarEnFunciones(elem);

							if (search != "false") {
								// si NO es falso, entonces signfica que si encontro la variable
								// con respecto a su funcion y el ambitoActual.

								isOk = true;
								tipoFinal = search;

							} else { // si no la encontro en una funcion que pueda ser que este llamada.

								String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

								boolean searchAmbito = BuscaTipoAmbito(elem);

								if (tipo == "Error") { // si no la encontro globalmente, tirar error.

									String error = "";

									if (!searchAmbito) { // si tampoco la encontro en el ambito
										if (ambitoActual == "%Global") {
											error = "No se encontro una variable de ID " + valorHijo
													+ " declarada globalmente.";
										} else {
											error = "No se encontro una variable declarada " + valorHijo
													+ " en la funcion: " + ambitoActual + " ni declarada globalmente.";
										}

										// System.out.println(error);
										node.setMensaje(error);
										node.setValor("Error");
										node.setError(true);
										isOk = false;

									} else { // esta cheque porque la encontro en el ambito de la funcion
										String tipoVar = returnTipoAmbito(elem);
										tipoFinal = tipoVar;
										isOk = true;
									}

								} else {

									if (searchAmbito) { // si es Verdadero, la encontro en el ambito
										String tipoVar = returnTipoAmbito(elem);
										tipoFinal = tipoVar;
										isOk = true;

									} else {
										// Si solo la encontro globalmente:
										tipoFinal = tipo;
										isOk = true;

									}

								}

							} // fin else del search

						} // fin if hijoizq

						if (hijoDer.getEtiqueta() == "ID") {

							String valorHijo = hijoDer.getValor();

							ElementoTS elem = new ElementoTS();
							elem.setAmbito(ambitoActual);
							elem.setID(valorHijo);

							String search = buscarEnFunciones(elem);

							if (search != "false") {
								// si NO es falso, entonces signfica que si encontro la variable
								// con respecto a su funcion y el ambitoActual.

								isOk = true;
								tipoDer = search;

							} else { // si no la encontro en una funcion que pueda ser que este llamada.

								String tipo = returnTipoGlobal(valorHijo); // revisar en variables globales

								boolean searchAmbito = BuscaTipoAmbito(elem);

								if (tipo == "Error") { // si no la encontro globalmente, tirar error.

									String error = "";

									if (!searchAmbito) { // si tampoco la encontro en el ambito
										if (ambitoActual == "%Global") {
											error = "No se encontro una variable de ID " + valorHijo
													+ " declarada globalmente.";
										} else {
											error = "No se encontro una variable declarada " + valorHijo
													+ " en la funcion: " + ambitoActual + " ni declarada globalmente.";
										}

										// System.out.println(error);
										node.setMensaje(error);
										node.setValor("Error");
										node.setError(true);
										isOk = false;

									} else { // esta cheque porque la encontro en el ambito de la funcion
										String tipoVar = returnTipoAmbito(elem);
										tipoDer = tipoVar;
										isOk = true;
									}

								} else {

									if (searchAmbito) { // si es Verdadero, la encontro en el ambito
										String tipoVar = returnTipoAmbito(elem);
										tipoDer = tipoVar;
										isOk = true;

									} else {
										// Si solo la encontro globalmente:
										tipoDer = tipo;
										isOk = true;

									}

								}

							} // fin else del search

						} // fin if hijoder

						if (hijoIzq.getEtiqueta() == "INTEGER") {
							tipoFinal = "INTEGER";
							isOk = true;

						} else if (hijoIzq.getEtiqueta() == "CHAR") {
							tipoFinal = "CHAR";
							isOk = true;

						}

						if (hijoDer.getEtiqueta() == "INTEGER") {
							tipoDer = "INTEGER";
							isOk = true;

						} else if (hijoDer.getEtiqueta() == "CHAR") {
							tipoDer = "CHAR";
							isOk = true;

						}

						if (isOk) {
							if (hijoDer.getEtiqueta() != "ID") {// si viene cualquier otra cosa en la derecha
								tipoDer = hijoDer.getEtiqueta();
							}
							if ((tipoFinal == "INTEGER") || (tipoFinal == "CHAR")) { // solo se pueden tener
																						// ints/chars

								// ahora revisar el nodo derecho

								if ((tipoDer == "INTEGER") || (tipoDer == "CHAR")) {
									String tipoNodoDer = tipoDer;
									String ValorSubido = "";

									switch (tipoNodoDer) {
									case "INTEGER": {
										String valorDerecho = hijoDer.getValor();
										ValorSubido = hijoIzq.getValor() + " " + oprel.getValor() + " " + valorDerecho;
										node.setValor(ValorSubido);
										break;
									}
									case "CHAR": {
										String valorDerecho = "\'" + hijoDer.getValor() + "\'";
										ValorSubido = hijoIzq.getValor() + oprel.getValor() + valorDerecho;
										node.setValor(ValorSubido);
										break;
									}
									}
								} else {
									node.setMensaje("La variable " + hijoDer.getValor()
											+ " adentro de un IF tiene que ser ya sea INTEGER o CHAR.");
									node.setValor("Error");
									node.setError(true);
								}

							} else {

								node.setMensaje("La variable " + hijoIzq.getValor()
										+ " adentro de un IF tiene que ser ya sea INTEGER o CHAR.");
								node.setValor("Error");
								node.setError(true);
							}
						} else {
							//
						}
					}

				} // fin if del size
				else {
					Nodo hijo = hijos.get(0);

					if (hijo.getError()) {
						node.setError(true);
						node.setValor("Error");
						node.setMensaje(hijo.getMensaje());

					} else {
						node.setValor(hijo.getValor());
					}
				}
				break;
			} // fin case expresion
			case "expresion_parentesis": {

				ArrayList<Nodo> hijos = node.getHijos();

				if (hijos.size() != 1) {
					Nodo hijoIzq = hijos.get(0);
					Nodo oprel = hijos.get(1);
					Nodo hijoDer = hijos.get(2);

					if (hijoIzq.getEtiqueta() == hijoDer.getEtiqueta()) {

						if (hijoIzq.getError() || hijoDer.getError()) {
							if (hijoIzq.getError() && hijoDer.getError()) {

							} else {
								if (hijoIzq.getError()) {

								} else if (hijoDer.getError()) {

								}
							}
						} else {
							String val = "(" + hijoIzq.getValor() + " " + oprel.getValor() + " " + hijoDer.getValor()
									+ ")";

							node.setValor(val);

						}

					}
				} else {
					Nodo hijo = hijos.get(0);

					if (hijo.getError()) {
						node.setError(true);
						node.setValor("Error");
						node.setMensaje(hijo.getMensaje());

					} else {
						node.setValor("(" + hijo.getValor() + ")");
					}
				}

				break;
			} // fin case expresion_parentesis
			default:
				break;
			}
		}
	}

	private static String recorrido(Nodo raiz) {
		String cuerpo = "";
		for (Nodo child : raiz.hijos) {
			if (!(child.getEtiqueta().equals("vacio"))) {
				cuerpo += "\"" + raiz.getId() + ". " + raiz.getEtiqueta() + " = " + raiz.getValor() + "\"->\""
						+ child.getId() + ". " + child.getEtiqueta() + " = " + child.getValor() + "\"" + "\n";
				cuerpo += recorrido(child);
			}
		}
		return cuerpo;
	}

	private static void Graficar(String cadena) {

		FileWriter fw = null;
		PrintWriter pw = null;
		String archivo = "AST.dot";
		try {
			fw = new FileWriter(archivo);
			pw = new PrintWriter(fw);
			pw.println("digraph G {");
			pw.println(cadena);
			pw.println("\n}");
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		try {
			String cmd = "dot -Tpng AST.dot -o fotoAST.png";
			Runtime.getRuntime().exec(cmd);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	// metodos para comprobacion de tipos y ambito
	// -----------------------------------------------------------
	public static String buscarEnFunciones(ElementoTS elemento) {

		for (ElementoTS funcion : ArregloFunciones) {
			if (ambitoActual == funcion.getAmbito()) {
				for (ElementoTS parametroFuncion : funcion.getParametros()) {
					if (elemento.getID().equals(parametroFuncion.getID())
							&& elemento.getAmbito().equals(parametroFuncion.getAmbito())) {
						return parametroFuncion.getTipo();
					}
				}
			}

		}
		return "false";
	}

	public static boolean buscarExistenciaEnFunciones(ElementoTS elemento) {

		for (ElementoTS funcion : ArregloFunciones) {
			if (ambitoActual == funcion.getAmbito()) {
				for (ElementoTS parametroFuncion : funcion.getParametros()) {
					if (elemento.getID().equals(parametroFuncion.getID())
							&& elemento.getAmbito().equals(parametroFuncion.getAmbito())) {
						return true;
					}
				}
			}

		}
		return false;
	}

	public static boolean RevisarSiEsFuncion(ElementoTS elem) {
		for (ElementoTS elementoTS : ArregloFunciones) {
			if (elementoTS.getID().equals(elem.getID()) && elementoTS.getAmbito().equals(elem.getAmbito())) {
				return true;
			}

		}
		return false;
	}

	public static String returnTipoGlobal(String id) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(id) && element.getAmbito().equals("%Global")) {
				return element.getTipo();
			}
		}
		return "Error";
	}

	public static String returnTipoAmbito(ElementoTS id) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(id.getID()) && element.getAmbito().equals(id.getAmbito())) {

				String tipo = element.getTipo();
				return tipo;

			}
		}
		return "Error";
	}

	public static String returnTipo(String id) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(id)) {
				return element.getTipo();
			}
		}
		return "Error";
	}

	public static String returnTipoFuncion(ElementoTS elem) {

		for (ElementoTS element : ArregloFunciones) {
			if (element.getID().equals(elem.getID()) && element.getAmbito().equals(elem.getAmbito())) {
				return element.getTipo();
			}
		}
		return "Error";
	}

	public static String returnTipoRecord(String id) {
		String retorno = "";
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(id)) {

				switch (element.getTipo()) {
				case "STRING":
				case "INTEGER":
				case "REAL":
				case "BOOLEAN":
				case "CHAR":
				case "RECORD":
					break;
				default:
					retorno = element.getTipo();
					break;
				}
				break;
			}
		}
		if (retorno.equals("")) {
			return "Error";
		} else {
			return retorno;
		}
	}

	public static ElementoTS returnElementoTS(String ID, String tipo) {

		for (ElementoTS element : ArregloSimbolos) {

			boolean ok = false;

			if(element.getAmbito().equals("%Global")){
				ok = true;
			}
			if (element.getID().equals(ID) && element.getTipo().equals(tipo)
					&& ( (element.getAmbito().equals(ambitoActual) || ok ) ) ) {
				return element;
			}
		}
		return null;
	}

	public static boolean BuscaTipo(ElementoTS elemento) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(elemento.getID()) && element.getTipo().equals(elemento.getTipo())) {
				return false;
			}
		}
		return true;
	}

	public static boolean BuscaTipoAmbito(ElementoTS elemento) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(elemento.getID()) && element.getAmbito().equals(elemento.getAmbito())) {
				return true;
			}
		}
		return false;
	}

	public static boolean BuscaTipoAmbitoActual(ElementoTS elemento) {
		for (ElementoTS element : ArregloSimbolos) {
			boolean ok = false;
			if(elemento.getAmbito().equals("%Global")){
				ok = true;
			}
			if (element.getID().equals(elemento.getID()) &&

			(elemento.getAmbito().equals(ambitoActual) || ok)
			) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkRecordVarConID(ElementoTS record, String IDElemento) {
		for (ElementoTS element : record.getRecordVars()) {
			if (element.getID().equals(IDElemento)) {
				return false;
			}
		}
		return true;
	}

	public static String returnTipoVariableRecord(ElementoTS record, String id) {
		for (ElementoTS element : record.getRecordVars()) {
			if (element.getID().equals(id)) {
				return element.getTipo();
			}
		}
		return "Error";
	}

	public static boolean buscarExistenciaID(ElementoTS elemento) {
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(elemento.getID()) && element.getAmbito().equals(elemento.getAmbito())) {
				return true;
			}
		}
		return false;
	}

	public static ElementoTS convertirVariableARecord(ElementoTS elemento, String ID) {
		// primero buscar el tipo record
		ElementoTS record = new ElementoTS();
		for (ElementoTS element : ArregloSimbolos) {
			if (element.getID().equals(ID) && element.getTipo().equals("RECORD")) {
				record = element;
				break;
			}
		}

		// convertir el elemento mandado a Record
		elemento.setRecordVars(record.getRecordVars());

		return elemento;
	}

	public static void ImprimirTS1() {

		System.out.println("Tabla de Simbolos: ");
		System.out.println("==============================");

		for (int i = 0; i < ArregloSimbolos.size(); i++) {
			ElementoTS elem = new ElementoTS();
			elem = ArregloSimbolos.get(i);
			System.out.println(String.format("      " + "| Indice: %d | ID: %s | TIPO: %s | AMBITO: %s |", i,
					elem.getID(), elem.getTipo(), elem.getAmbito()));
		}
		System.out.println("==============================");
	}

	public static void ImprimirTSFunc() {

		System.out.println("Tabla de Simbolos (Funciones): ");
		System.out.println("==============================");

		for (int i = 0; i < ArregloFunciones.size(); i++) {
			ElementoTS elem = new ElementoTS();
			elem = ArregloFunciones.get(i);

			String acum = "[";

			int sizeAr = ArregloFunciones.get(i).getParametros().size();

			for (int j = 0; j < sizeAr; j++) {
				ElementoTS param = new ElementoTS();
				param = ArregloFunciones.get(i).getParametros().get(j);
				if (j == sizeAr - 1) {
					acum += param.getID();
				} else {
					acum += param.getID() + ", ";
				}
			}

			acum += "]";

			System.out.println(String.format("      " + "| Indice: %d | ID: %s | TIPO: %s | AMBITO: %s | PARAMS: %s |",
					i, elem.getID(), elem.getTipo(), elem.getAmbito(), acum));
		}

		System.out.println("==============================");
	}

	public static boolean buscarFuncionConParametros(ElementoTS param, boolean esFuncion) {

		// recorrer el arreglo de funciones

		ElementoTS funcion = new ElementoTS();
		funcion = null;

		// Manejo de Errores de Llamada de funcion:
		FunctionEncontrada = ""; // resetear valor

		// encontrar la funcion como tal en el arreglo de funciones

		for (ElementoTS elementoTS : ArregloFunciones) {
			if (elementoTS.getID().equals(param.getID())) {

				// ahora revisar si es un proc o funcion

				String tipoFuncion = elementoTS.getTipo();

				if (tipoFuncion == null && !esFuncion) {
					/*
					 * aqui lo que se comprueba si el elemento que esta iterando del arreglo
					 * funciones si es un procedimiento. el tipo es null si es un procedimiento
					 * ademas hay que ver si la funcion que viene como parametro NO es una funcion
					 *
					 */
					funcion = elementoTS;

				} else if (tipoFuncion != null && esFuncion) {
					// si el iterable tiene tipoVAR significa que es una funcion, ademas revisar si
					// el param esfuncion
					funcion = elementoTS;
				}

			}
		}

		if (funcion == null) { // si no encontro nada
			ErrorFuncion = "No se encontro una funcion o procedimiento declarado " + param.getID();
			return false;
		} else {

			// revisar si los parametros son iguales

			// primero revisar si son de igual tamanio

			int size = param.getParametros().size();
			//System.out.println("parametros: " + Integer.toString(size));
			int size2 = funcion.getParametros().size();
			//System.out.println("parametros: " + Integer.toString(size2));

			// manejo de errores: (Cargar el string con la funcion y sus argumentos)

			FunctionEncontrada = funcion.getID() + "(";

			for (int i = 0; i < funcion.getParametros().size(); i++) {

				if (i == funcion.getParametros().size() - 1) {
					FunctionEncontrada = FunctionEncontrada + funcion.getParametros().get(i).getTipo() + ")";
				} else {
					FunctionEncontrada = FunctionEncontrada + funcion.getParametros().get(i).getTipo() + ", ";
				}
			}

			// ahora revisar el tamanio de ambos

			if (size != size2) {

				if (esFuncion) {
					if (ambitoActual == "%Global") {
						ErrorFuncion = "La funcion " + param.getID()
								+ " tiene parametros incorrectos en el main principal.";
					} else {
						ErrorFuncion = "La funcion " + param.getID()
								+ " tiene parametros incorrectos, esta siendo llamada adentro de la funcion: "
								+ ambitoActual;
					}

				} else {
					if (ambitoActual == "%Global") {
						ErrorFuncion = "El procedimiento " + param.getID()
								+ " tiene parametros incorrectos en el main principal.";
					} else {
						ErrorFuncion = "El procedimiento " + param.getID()
								+ " tiene parametros incorrectos, esta siendo llamada adentro de la funcion: "
								+ ambitoActual;
					}
				}

				return false;

			} else {
				for (int i = 0; i < size; i++) {
					if ((!(param.getParametros().get(i).getTipo().equals(funcion.getParametros().get(i).getTipo())))) {
						// si aunque un parametro NO sea igual, esque esta malo, revisar tipo

						if (ambitoActual == "%Global") {
							ErrorFuncion = "No se encontro una funcion o procedimiento declarado " + param.getID()
									+ " que tenga los mismos parametros que se mandaron en el begin principal.";
						} else {
							ErrorFuncion = "No se encontro una funcion o procedimiento declarado " + param.getID()
									+ " que tenga los mismos parametros que se mandaron en la funcion/proc: "
									+ ambitoActual;
						}

						return false;
					}
				}
			}

		}

		if (esFuncion) {
			tipoFuncion = funcion.getTipo();
		} else {
			tipoFuncion = "PROCEDURE";
		}

		return true;
	}


  // Obtiene la posición del primer temporal desocupado que encuentra, sino retorna -1. [->]
  private static int temporalDesocupado() {
	for (int i = 0; i < temporales.length; i++) {
		if (!temporales[i]) {
			return i;
		}
	}
	return -1;
}

// Obtiene la posición del primer argumento desocupado que encuentra, sino retorna -1. [->]
private static int argumentoDesocupado() {
  for (int i = 0; i < argumentos.length; i++) {
	  if (!argumentos[i]) {
		  return i;
	  }
  }
  return -1;
}

// Obtiene la posición del primer temporal ocupado que encuentra, sino retorna -1. [->]
private static int temporalOcupado() {
  for (int i = 0; i < temporales.length; i++) {
	  if (temporales[i]) {
		  return i;
	  }
  }
  return -1;
}


// Obtiene la posición del primer temporal ocupado que encuentra, sino retorna -1. [<-]
private static int temporalOcupadoRL() {
    for (int i = temporales.length-1; i >= 0; i--) {
        if (temporales[i]) {
            return i;
        }
    }
    return -1;
}

  // Obtiene la posición del primer argumento ocupado que encuentra, sino retorna -1. [<-]
private static int argumentoOcupado() {
    for (int i = argumentos.length-1; i >= 0; i--) {
        if (argumentos[i]) {
            return i;
        }
    }
    return -1;
}

 // Encuentra la posición de un mensaje repetido en el ArrayList de mensajes, sino retorna -1.
 private static int mensajeRepetido(String msg) {
	for (String mensaje : mensajes) {
		if (msg.equals(mensaje)) {
			return mensajes.indexOf(mensaje);
		}
	}
	return -1;
}
private static void liberalTemporal(int indice) {
  temporales[indice] = false;
}
private static int nuevoTeporal() {
  for (int i = 0; i < temporales.length; i++) {
    if (!temporales[i]) {
      temporalActual=i;
      temporales[i] = true;
      return i;
    }
  }
  liberalTemporal(0);
  return 0;
}
private static Boolean isTemporal(String value) {
  return temporalesIntermedio.indexOf(value) != -1;
}
private static boolean isNumeric(String cadena){
	try {
		Integer.parseInt(cadena);
		return true;
	} catch (NumberFormatException nfe){
		return false;
	}
}
// ########## FIN FUNCIONES DE APOYO PARA LA GENERACIÓN DE CÓDIGO FINAL ##########

private static String temporalNuevo() { return "%t" + temporal++; }
	//creacion del codigo final
	public static void codigoFinal(){
		System.out.println("Codigo Final en MIPS");
		System.out.println("\n");
		MIPS.add("	.data");
		//final para los elementos globales en la TS
		for(ElementoTS elementoTS: ArregloSimbolos){
			String idEnTabla = elementoTS.getID();
			String ambitoEnTabla = elementoTS.getAmbito();
			String tipoEnTabla = elementoTS.getTipo();

			if(ambitoEnTabla.equals("%Global")) {
				if(tipoEnTabla.equals("char")){
					MIPS.add("_" + idEnTabla + ":   .space 2");
				}else{
					MIPS.add("_" + idEnTabla + ":   .word 0");
				}
			}
		}
			//final para los mensajes
			for(Cuadruplo cuadruplo: tablaCuadruplos){
				String operadorCuad = cuadruplo.getOperador();
				String arg1Cuad = cuadruplo.getArgumento1();
				String arg2Cuad = cuadruplo.getArgumento2();
				if(operadorCuad.equals("printf") && !arg1Cuad.equals("_")) {
					arg1Cuad = arg1Cuad.replaceAll("\"", "\"");
					if(arg2Cuad == "_"){
						//escribe el mensaje en el codigo final
						int position = mensajeRepetido(arg1Cuad);
						if(position == -1){
							//System.out.println("aqui entre");
              mensajes.add(arg1Cuad); //guarda el mensaje en el arreglo de mensaje en cuyo caso no exista
							MIPS.add("_msg" + (mensajes.size() - 1) + ": " +" .asciiz "+ arg1Cuad );
						}else{/*nada pasa*/}

					}else{ //existe un segundo argumento
						//System.out.println("estoy aqui");
						int position = mensajeRepetido(arg1Cuad);
						if(position == -1){
							//System.out.println("ahora aqui");
							mensajes.add(arg1Cuad);
							MIPS.add("_msg" + (mensajes.size() - 1) + ": " + arg1Cuad + " .asciiz ");
						}else{/*nada pasa*/}

						if(arg2Cuad.charAt(0) == '\''){
							//System.out.println("entre a este lugar");
							arg2Cuad = arg2Cuad.replaceAll("\"", "\"");
							int position2 = mensajeRepetido(arg2Cuad);
							if(position2 == -1){
								mensajes.add(arg2Cuad);
								MIPS.add("_msg" + (mensajes.size() - 1) + ": " + arg2Cuad + " .asciiz");
							}else{/*nada pasa*/}
						}
					}
				}
			}

		MIPS.add("\n");
		//.text
		MIPS.add("	.text");
		MIPS.add("	.globl main");
		MIPS.add("\n");
		MIPS.add("main: ");

		for(ElementoTS elementoTSArreglo : ArregloFunciones){
			if(elementoTSArreglo.getAmbito() != ambitoActual && elementoTSArreglo.getAmbito() != elementoTSArreglo.getID()){
				//System.out.println("Los parametros son: " + elementoTSArreglo.getParametros());
			}
		}
    //inicio de cuerpo de codigo final

    for(Cuadruplo cuadruplo: TablaCuadruplo.getTablaCuadruplo()){
      String operadorCuad = cuadruplo.getOperador();
      String arg1Cuad = cuadruplo.getArgumento1();
      String arg2Cuad = cuadruplo.getArgumento2();
      String respuesta = cuadruplo.getRespuesta();

      if (operadorCuad.equals("printf") && !arg1Cuad.equals("_")) {
        MIPS.add("  li  $v0,  4");
        MIPS.add("  la  $a0,  _msg"+mensajeRepetido(arg1Cuad));
        MIPS.add("  syscall");
        MIPS.add("");
      } else if (operadorCuad.equals("printf") && arg1Cuad.equals("_")) {
        MIPS.add("  li  $v0,  1");
        MIPS.add("  la  $a0,  _"+arg2Cuad);
        MIPS.add("  syscall");
        MIPS.add("");
      } else if (operadorCuad.equals("scanf")) {
        MIPS.add("  li  $v0,  5");
        MIPS.add("  syscall");
        MIPS.add("  sw  $v0,  _"+arg1Cuad);
        MIPS.add("");
      } else if (operadorCuad.equals("+") ||operadorCuad.equals("*") ||operadorCuad.equals("-") ||operadorCuad.equals("/")) {
        int primerTemporal;
        int segundoTemporal;
        if (!isTemporal(arg1Cuad)) {
          primerTemporal = nuevoTeporal();
          if (isNumeric(arg1Cuad)) {
            MIPS.add("  lw  $t"+primerTemporal+",  "+arg1Cuad);
          } else {
            MIPS.add("  lw  $t"+primerTemporal+",  _"+arg1Cuad);
          }
        } else {
          if (isTemporal(arg1Cuad) && isTemporal(arg2Cuad)) {
            primerTemporal = temporalActual-1;
          } else {
            primerTemporal = temporalActual;
          }

        }
        if (!isTemporal(arg2Cuad)) {
          segundoTemporal = nuevoTeporal();
          if (isNumeric(arg2Cuad)) {
            MIPS.add("  lw  $t"+segundoTemporal+", "+arg2Cuad);
          } else {
            MIPS.add("  lw  $t"+segundoTemporal+", _"+arg2Cuad);
          }
        } else  {
          segundoTemporal = temporalActual;
        }
        int resultadoTemporal = nuevoTeporal();
        String comando = "";
        if (operadorCuad.equals("+")) {
          comando="add";
        } else if (operadorCuad.equals("-")) {
          comando="sub";
        } else if (operadorCuad.equals("*")) {
          comando="mul";
        } else if (operadorCuad.equals("/")) {
          comando="div";
        }
        MIPS.add("  "+comando + " $t"+resultadoTemporal+",  $t"+primerTemporal+", $t"+segundoTemporal);
        liberalTemporal(primerTemporal);
        liberalTemporal(segundoTemporal);
        MIPS.add("");
      } else if (operadorCuad.equals("=")) {
        if (isTemporal(arg1Cuad)) {
          MIPS.add("  sw  $t"+temporalActual+",  _"+respuesta);
        } else {
          int primerTemporal = nuevoTeporal();
          if (isNumeric(arg1Cuad)) {
            MIPS.add("  lw  $t"+primerTemporal+",  "+arg1Cuad);
          } else {
            MIPS.add("  lw  $t"+primerTemporal+",  _"+arg1Cuad);
          }
          MIPS.add("  sw  $t"+primerTemporal+",  _"+respuesta);
          liberalTemporal(primerTemporal);
        }
        MIPS.add("");
      }
    }
    MIPS.add("li  $v0,  10");
    MIPS.add("syscall");

		//impresion del codigo final
		for(String codigo : MIPS){
			System.out.println(codigo);
		}
	}

	// -----------------------------------------------------------

}