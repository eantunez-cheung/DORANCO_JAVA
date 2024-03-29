package fr.doranco.ecommerce.metier;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import fr.doranco.ecommerce.cryptage.Cryptage;
import fr.doranco.ecommerce.entity.beans.CartePaiement;
import fr.doranco.ecommerce.entity.beans.Params;
import fr.doranco.ecommerce.entity.beans.User;
import fr.doranco.ecommerce.entity.dto.CartePaiementDto;
import fr.doranco.ecommerce.enums.AlgorithmesCryptagePrincipal;
import fr.doranco.ecommerce.model.dao.CartePaiementDao;
import fr.doranco.ecommerce.model.dao.ICartePaiementDao;
import fr.doranco.ecommerce.model.dao.IParamsDao;
import fr.doranco.ecommerce.model.dao.ParamsDao;
import fr.doranco.ecommerce.utils.Cookies;
import fr.doranco.ecommerce.utils.Dates;

public class CartePaiementMetier implements ICartePaiementMetier {

	private ICartePaiementDao cartePaiementDao = new CartePaiementDao();
	private IParamsDao paramsDao = new ParamsDao();
	
	@Override
	public void add(CartePaiementDto cartepaiementDto) throws Exception {
		if (cartepaiementDto == null
				|| cartepaiementDto.getNomProprietaire() == null || cartepaiementDto.getNomProprietaire().trim().isEmpty()
				|| cartepaiementDto.getPrenomProprietaire() == null || cartepaiementDto.getPrenomProprietaire().trim().isEmpty()
				|| cartepaiementDto.getNumero() == null || cartepaiementDto.getNumero().trim().isEmpty()
				|| cartepaiementDto.getDateFinValidite() == null || cartepaiementDto.getDateFinValidite().trim().isEmpty()
				|| cartepaiementDto.getCryptogramme() == null || cartepaiementDto.getCryptogramme().trim().isEmpty()) {
			throw new IllegalArgumentException("Les param�tre doivent �tre non nuls et non vides !");
		}
		
		Date dateFinValidite = Dates.convertStringToDateUtil(cartepaiementDto.getDateFinValidite());
		Date currentDate = Dates.convertDateTimeToDateUtil(LocalDate.now());
		if (currentDate.compareTo(dateFinValidite) > 0) {
			throw new IllegalArgumentException("La carte de paiement � expir� !");
		}
		
		Cookie cookie = Cookies.getCookie("user");
		User user = new User();
		user.setId(Integer.valueOf(cookie.getValue()));
		
		String algo = AlgorithmesCryptagePrincipal.DES.getAlgorithme();
		Params params = paramsDao.get(Params.class, 1);
		SecretKey key = new SecretKeySpec(params.getCleCryptage(), algo);
		
		CartePaiement cartePaiement = new CartePaiement();
		cartePaiement.setNomProprietaire(cartepaiementDto.getNomProprietaire());
		cartePaiement.setPrenomProprietaire(cartepaiementDto.getPrenomProprietaire());
		cartePaiement.setNumero(Cryptage.encrypt(algo, cartepaiementDto.getNumero(), key));
		cartePaiement.setDateFinValidite(dateFinValidite);
		cartePaiement.setCryptogramme(Cryptage.encrypt(algo, cartepaiementDto.getCryptogramme(), key));
		cartePaiement.setUser(user);
		
		
		cartePaiementDao.add(cartePaiement);
	}

	@Override
	public CartePaiementDto getCartepaiementDto(Integer id) throws Exception {
		CartePaiementDto cartepaiementDto = new CartePaiementDto();
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Le param�tre 'id' doit �tre sup�rieur � 0 !");
		}
		
		String algo = AlgorithmesCryptagePrincipal.DES.getAlgorithme();
		Params params = paramsDao.get(Params.class, 1);
		SecretKey key = new SecretKeySpec(params.getCleCryptage(), algo);
		
		CartePaiement cartePaiement = cartePaiementDao.get(CartePaiement.class, id);
		cartepaiementDto.setNomProprietaire(cartePaiement.getNomProprietaire());
		cartepaiementDto.setPrenomProprietaire(cartePaiement.getPrenomProprietaire());
		cartepaiementDto.setNumero(Cryptage.decrypt(algo, cartePaiement.getNumero(), key));
		cartepaiementDto.setDateFinValidite(Dates.convertDateUtilToString(cartePaiement.getDateFinValidite()));
		cartepaiementDto.setCryptogramme(Cryptage.decrypt(algo, cartePaiement.getCryptogramme(), key));
		return cartepaiementDto;
	}

	@Override
	public Set<CartePaiementDto> getCartespaiementDto() throws Exception {
		Set<CartePaiementDto> cartespaiementDto = new HashSet<CartePaiementDto>();
		Set<CartePaiement> cartespaiement = new HashSet<CartePaiement>(cartePaiementDao.getAll(CartePaiement.class));
		
		String algo = AlgorithmesCryptagePrincipal.DES.getAlgorithme();
		Params params = paramsDao.get(Params.class, 1);
		SecretKey key = new SecretKeySpec(params.getCleCryptage(), algo);
		
		for (CartePaiement cartePaiement : cartespaiement) {
			CartePaiementDto cartepaiementDto = new CartePaiementDto();
			cartepaiementDto.setNomProprietaire(cartePaiement.getNomProprietaire());
			cartepaiementDto.setPrenomProprietaire(cartePaiement.getPrenomProprietaire());
			cartepaiementDto.setNumero(Cryptage.decrypt(algo, cartePaiement.getNumero(), key));
			cartepaiementDto.setDateFinValidite(Dates.convertDateUtilToString(cartePaiement.getDateFinValidite()));
			cartepaiementDto.setCryptogramme(Cryptage.decrypt(algo, cartePaiement.getCryptogramme(), key));
			cartespaiementDto.add(cartepaiementDto);
		}
		return cartespaiementDto;
	}

	@Override
	public void remove(CartePaiementDto cartepaiementDto) throws Exception {
		if (cartepaiementDto == null
				|| cartepaiementDto.getNomProprietaire() == null || cartepaiementDto.getNomProprietaire().trim().isEmpty()
				|| cartepaiementDto.getPrenomProprietaire() == null || cartepaiementDto.getPrenomProprietaire().trim().isEmpty()
				|| cartepaiementDto.getNumero() == null || cartepaiementDto.getNumero().trim().isEmpty()
				|| cartepaiementDto.getDateFinValidite() == null || cartepaiementDto.getDateFinValidite().trim().isEmpty()
				|| cartepaiementDto.getCryptogramme() == null || cartepaiementDto.getCryptogramme().trim().isEmpty()) {
			throw new IllegalArgumentException("Les param�tre doivent �tre non nuls et non vides !");
		}
		String algo = AlgorithmesCryptagePrincipal.DES.getAlgorithme();
		Params params = paramsDao.get(Params.class, 1);
		SecretKey key = new SecretKeySpec(params.getCleCryptage(), algo);
		
		CartePaiement cartePaiement = new CartePaiement();
		cartePaiement.setNomProprietaire(cartepaiementDto.getNomProprietaire());
		cartePaiement.setPrenomProprietaire(cartepaiementDto.getPrenomProprietaire());
		cartePaiement.setNumero(Cryptage.encrypt(algo, cartepaiementDto.getNumero(), key));
		cartePaiement.setDateFinValidite(Dates.convertStringToDateUtil(cartepaiementDto.getDateFinValidite()));
		cartePaiement.setCryptogramme(Cryptage.encrypt(algo, cartepaiementDto.getCryptogramme(), key));
		
		cartePaiementDao.remove(cartePaiement);
	}

}
