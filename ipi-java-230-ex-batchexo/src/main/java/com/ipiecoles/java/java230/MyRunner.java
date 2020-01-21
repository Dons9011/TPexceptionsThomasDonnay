package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.CommercialRepository;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

	private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
	private static final String REGEX_NOM = ".*";
	private static final String REGEX_PRENOM = ".*";
	private static final int NB_CHAMPS_MANAGER = 5;
	private static final int NB_CHAMPS_TECHNICIEN = 7;
	private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
	private static final int NB_CHAMPS_COMMERCIAL = 7;

	@Autowired
	private EmployeRepository employeRepository;

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private CommercialRepository commercialRepository;

	private List<Employe> employes = new ArrayList<Employe>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run(String... strings) throws Exception {
		String fileName = "employes.csv";
		readFile(fileName);
		// readFile(strings[0]);
	}

	/**
	 * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en
	 * BDD
	 * 
	 * @param fileName Le nom du fichier (à mettre dans src/main/resources)
	 * @return une liste contenant les employés à insérer en BDD ou null si le
	 *         fichier n'a pas pu être le
	 */
	public List<Employe> readFile(String fileName) throws Exception {
		Stream<String> stream;
		stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
		// TODO
		Integer i = 0;

		for (String ligne : stream.collect(Collectors.toList())) {
			i++;
			try {
				processLine(ligne);

			} catch (BatchException e) {
				System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);
			}
		}
		System.out.println(employes);
		return employes;
	}

	/**
	 * Méthode qui regarde le premier caractère de la ligne et appelle la bonne
	 * méthode de création d'employé
	 * 
	 * @param ligne la ligne à analyser
	 * @throws BatchException si le type d'employé n'a pas été reconnu
	 */
	private void processLine(String ligne) throws BatchException {
		if (!ligne.matches("^[MTC]{1}.*")) {
			throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
		}

		if (ligne.matches("^[M]{1}.*")) {
			processManager(ligne);
		}

		if (ligne.matches("^[C]{1}.*")) {
			processCommercial(ligne);
		}

		if (ligne.matches("^[T]{1}.*")) {
			processTechnicien(ligne);
		}

	}

	/**
	 * Méthode qui crée un Commercial à partir d'une ligne contenant les
	 * informations d'un commercial et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
	 * @throws BatchException s'il y a un problème sur cette ligne
	 */
	private void processCommercial(String ligneCommercial) throws BatchException {

		Commercial com = new Commercial();

		String[] ligneco = ligneCommercial.split(",");
		// nombre de champs
		if (NB_CHAMPS_COMMERCIAL == ligneco.length && ligneco[0].matches(REGEX_MATRICULE)) {

			// matricule
			try {
				String strMatriculeCom = ligneco[0];
				com.setMatricule(strMatriculeCom);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le matricule : " + ligneco[0]);
			}
			;

			// nom et prenom
			try {
				String strPrenomCom = ligneco[1];
				String strNomCom = ligneco[2];
				com.setPrenom(strPrenomCom);
				com.setNom(strNomCom);
			} catch (Exception e) {
				throw new BatchException("Probleme avec les nom et prenom : " + ligneco[1] + "et " + ligneco[2]);
			}
			;

			// date embauche
			try {
				LocalDate d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(ligneco[3]);
				com.setDateEmbauche(d);
			} catch (Exception e) {
				throw new BatchException("Probleme avec la date d'embauche : " + ligneco[3]);
			}
			;

			// Salaire
			try {
				Double strSalaireCom = Double.parseDouble(ligneco[4]);
				com.setSalaire(strSalaireCom);
			} catch (NumberFormatException p) {
				throw new BatchException("Probleme avec le parsing du salaire : " + ligneco[4]);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le salaire : " + ligneco[4]);
			}
			;

			// CA
			try {
				Double strCaCom = Double.parseDouble(ligneco[5]);
				com.setCaAnnuel(strCaCom);
			} catch (NumberFormatException p) {
				throw new BatchException("Probleme avec le parsing du CA : " + ligneco[5]);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le CA du commercial : " + ligneco[5]);
			}
			;

			employes.add(com);

		} else {
			throw new BatchException("Probleme avec le nombre de champs ou le regex : " + ligneco);
		}
	}

	/**
	 * Méthode qui crée un Manager à partir d'une ligne contenant les informations
	 * d'un manager et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneManager la ligne contenant les infos du manager à intégrer
	 * @throws BatchException s'il y a un problème sur cette ligne
	 */
	private void processManager(String ligneManager) throws BatchException {

		Manager man = new Manager();

		String[] ligneman = ligneManager.split(",");

		// nombre de champs
		if (NB_CHAMPS_MANAGER == ligneman.length && ligneman[0].matches(REGEX_MATRICULE)) {

			// matricule
			try {
				String strMatriculeman = ligneman[0];
				man.setMatricule(strMatriculeman);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le matricule : " + ligneman[0]);
			}
			;

			// nom et prenom
			try {
				String strPrenomman = ligneman[1];
				String strNomman = ligneman[2];
				man.setPrenom(strPrenomman);
				man.setNom(strNomman);
			} catch (Exception e) {
				throw new BatchException("Probleme avec les nom et prenom : " + ligneman[1] + "et " + ligneman[2]);
			}
			;

			// date embauche
			try {
				LocalDate d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(ligneman[3]);
				man.setDateEmbauche(d);
			} catch (Exception e) {
				throw new BatchException("Probleme avec la date d'embauche : " + ligneman[3]);
			}
			;

			// Salaire
			try {
				Double strSalaireman = Double.parseDouble(ligneman[4]);
				man.setSalaire(strSalaireman);
			} catch (NumberFormatException p) {
				throw new BatchException("Probleme avec le parsing du salaire : " + ligneman[4]);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le salaire : " + ligneman[4]);
			}
			;

			// Equipe
			try {
				HashSet<Technicien> strEquipeman = new HashSet<Technicien>();
				man.setEquipe(strEquipeman);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le HashSet de l'equipe ");
			}
			;

			employes.add(man);

		} else {
			throw new BatchException("Probleme avec le nombre de champs ou le regex : " + ligneman);
		}
	}

	/**
	 * Méthode qui crée un Technicien à partir d'une ligne contenant les
	 * informations d'un technicien et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
	 * @throws BatchException s'il y a un problème sur cette ligne
	 */
	private void processTechnicien(String ligneTechnicien) throws BatchException {

		Technicien tech = new Technicien();

		String[] lignetech = ligneTechnicien.split(",");
		// nombre de champs
		if (NB_CHAMPS_TECHNICIEN == lignetech.length && lignetech[0].matches(REGEX_MATRICULE)) {

			// matricule
			try {
				String strMatriculetech = lignetech[0];
				tech.setMatricule(strMatriculetech);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le matricule : " + lignetech[0]);
			}
			;

			// nom et prenom
			try {
				String strPrenomtech = lignetech[1];
				String strNomtech = lignetech[2];
				tech.setPrenom(strPrenomtech);
				tech.setNom(strNomtech);
			} catch (Exception e) {
				throw new BatchException("Probleme avec les nom et prenom : " + lignetech[1] + "et " + lignetech[2]);
			}
			;

			// date embauche
			try {
				LocalDate d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(lignetech[3]);
				tech.setDateEmbauche(d);
			} catch (Exception e) {
				throw new BatchException("Probleme avec la date d'embauche : " + lignetech[3]);
			}
			;

			// Grade
			try {
				Integer strGradetech = Integer.parseInt(lignetech[5]);
				tech.setGrade(strGradetech);
			} catch (NumberFormatException p) {
				throw new BatchException("Probleme avec le parsing du garde : " + lignetech[5]);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le grae : " + lignetech[5]);
			}
			;

			// Salaire
			try {
				Double strSalairetech = Double.parseDouble(lignetech[4]);
				tech.setSalaire(strSalairetech);
			} catch (NumberFormatException p) {
				throw new BatchException("Probleme avec le parsing du salaire : " + lignetech[4]);
			} catch (Exception e) {
				throw new BatchException("Probleme avec le salaire : " + lignetech[4]);
			}
			;

			// Manager
			try {
				String strManagertech = lignetech[6];
				Manager managerOftech = managerRepository.findByMatricule(strManagertech);
				if (strManagertech.matches(REGEX_MATRICULE_MANAGER)) {
					if (managerOftech!=null) {
						tech.setManager(managerOftech);
						Manager managerOftechBidon = managerRepository.findByMatricule(strManagertech);
					}else {
						throw new BatchException(
								"Le manager " + lignetech[6] + " n'existe pas en BDD");
					}
				} else {
					throw new BatchException(
							"Probleme avec le format du matricule Manager du technicien : " + lignetech[6]);
				}
			} catch (Exception e) {
				throw new BatchException("Probleme avec le Manager du technicien : " + lignetech[6]);
			}
			;

			employes.add(tech);

		} else {
			throw new BatchException("Probleme avec le nombre de champs ou le regex : " + lignetech);
		}
	}
}
