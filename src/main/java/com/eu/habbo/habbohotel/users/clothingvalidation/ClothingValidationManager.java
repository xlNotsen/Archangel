package com.eu.habbo.habbohotel.users.clothingvalidation;

import com.eu.habbo.habbohotel.users.Habbo;
import gnu.trove.TIntCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Slf4j
public class ClothingValidationManager {

    public static String FIGUREDATA_URL = "";
    public static boolean VALIDATE_ON_HC_EXPIRE = false;
    public static boolean VALIDATE_ON_LOGIN = false;
    public static boolean VALIDATE_ON_CHANGE_LOOKS = false;
    public static boolean VALIDATE_ON_MIMIC = false;
    public static boolean VALIDATE_ON_MANNEQUIN = false;
    public static boolean VALIDATE_ON_FBALLGATE = false;

    private static final Figuredata FIGUREDATA = new Figuredata();

    /**
     * Parses the new figuredata.xml file
     * @param newUrl URI of figuredata.xml file. Can be a file path or URL
     */
    public static void reloadFiguredata(String newUrl) {
        try {
            FIGUREDATA.parseXML(newUrl);
        } catch (Exception e) {
            VALIDATE_ON_HC_EXPIRE = false;
            VALIDATE_ON_LOGIN = false;
            VALIDATE_ON_CHANGE_LOOKS = false;
            VALIDATE_ON_MIMIC = false;
            VALIDATE_ON_MANNEQUIN = false;
            VALIDATE_ON_FBALLGATE = false;
            log.error("Caught exception", e);
        }
    }

    /**
     * Validates a figure string on a given user
     * @param habbo User to validate
     * @return Cleaned figure string
     */
    public static String validateLook(Habbo habbo) {
        return validateLook(habbo.getHabboInfo().getLook(), habbo.getHabboInfo().getGender().name(), habbo.getHabboStats().hasActiveClub(), habbo.getInventory().getWardrobeComponent().getClothingSets());
    }

    /**
     * Validates a given figure string and gender on a given user
     * @param habbo User to validate
     * @param look Figure string
     * @param gender Gender (M/F)
     * @return Cleaned figure string
     */
    public static String validateLook(Habbo habbo, String look, String gender) {
        return validateLook(look, gender, habbo.getHabboStats().hasActiveClub(), habbo.getInventory().getWardrobeComponent().getClothingSets());
    }

    /**
     * Validates a given figure string against a given gender
     * @param look Figure string
     * @param gender Gender (M/F)
     * @return Cleaned figure string
     */
    public static String validateLook(String look, String gender) {
        return validateLook(look, gender, false, new TIntHashSet());
    }

    /**
     * Validates a given figure string against a given gender with club clothing option
     * @param look Figure string
     * @param gender Gender (M/F)
     * @param isHC Boolean indicating if club clothing is permitted
     * @return Cleaned figure string
     */
    public static String validateLook(String look, String gender, boolean isHC) {
        return validateLook(look, gender, isHC, new TIntHashSet());
    }

    /**
     * Validates a figure string with all available options
     * @param look Figure string
     * @param gender Gender (M/F)
     * @param isHC Boolean indicating if club clothing is permitted
     * @param ownedClothing Array of owned clothing set IDs. If sellable and setId not in this array clothing will be removed
     * @return Cleaned figure string
     */
    public static String validateLook(String look, String gender, boolean isHC, TIntCollection ownedClothing) {
        if(FIGUREDATA.getPalettes().size() == 0 || FIGUREDATA.getSettypes().size() == 0)
            return look;

        String[] newLookParts = look.split(Pattern.quote("."));
        ArrayList<String> lookParts = new ArrayList<>();

        THashMap<String, String[]> parts = new THashMap<>();

        // add mandatory settypes
        for(String lookpart : newLookParts) {
            if (lookpart.contains("-")) {
                String[] data = lookpart.split(Pattern.quote("-"));
                FiguredataSettype settype = FIGUREDATA.getSettypes().get(data[0]);
                if(settype != null) {
                    parts.put(data[0], data);
                }
            }
        }

        FIGUREDATA.getSettypes().entrySet().stream().filter(x -> !parts.containsKey(x.getKey())).forEach(x ->
        {
            FiguredataSettype settype = x.getValue();

            if(gender.equalsIgnoreCase("M") && !isHC && !settype.isMandatoryMale0())
                return;

            if(gender.equalsIgnoreCase("F") && !isHC && !settype.isMandatoryFemale0())
                return;

            if(gender.equalsIgnoreCase("M") && isHC && !settype.isMandatoryMale1())
                return;

            if(gender.equalsIgnoreCase("F") && isHC && !settype.isMandatoryFemale1())
                return;

            parts.put(x.getKey(), new String[] { x.getKey() });
        });


        parts.forEach((key, data) -> {
            try {
                if (data.length >= 1) {
                    FiguredataSettype settype = FIGUREDATA.getSettypes().get(data[0]);
                    if (settype == null) {
                        //throw new Exception("Set type " + data[0] + " does not exist");
                        return;
                    }

                    FiguredataPalette palette = FIGUREDATA.getPalettes().get(settype.getPaletteId());
                    if (palette == null) {
                        throw new Exception("Palette " + settype.getPaletteId() + " does not exist");
                    }

                    int setId;
                    FiguredataSettypeSet set;

                    setId = Integer.parseInt(data.length >= 2 ? data[1] : "-1");
                    set = settype.getSet(setId);

                    if (set == null || (set.isClub() && !isHC) || !set.isSelectable() || (set.isSellable() && !ownedClothing.contains(set.getId())) || (!set.getGender().equalsIgnoreCase("U") && !set.getGender().equalsIgnoreCase(gender))) {
                        if (gender.equalsIgnoreCase("M") && !isHC && !settype.isMandatoryMale0())
                            return;

                        if (gender.equalsIgnoreCase("F") && !isHC && !settype.isMandatoryFemale0())
                            return;

                        if (gender.equalsIgnoreCase("M") && isHC && !settype.isMandatoryMale1())
                            return;

                        if (gender.equalsIgnoreCase("F") && isHC && !settype.isMandatoryFemale1())
                            return;

                        set = settype.getFirstNonHCSetForGender(gender);
                        setId = set.getId();
                    }

                    ArrayList<String> dataParts = new ArrayList<>();

                    int color1 = -1;
                    int color2 = -1;

                    if (set.isColorable()) {
                        color1 = data.length >= 3 ? Integer.parseInt(data[2]) : -1;
                        FiguredataPaletteColor color = palette.getColor(color1);
                        if (color == null || (color.isClub() && !isHC)) {
                            color1 = palette.getFirstNonHCColor().getId();
                        }
                    }

                    if (data.length >= 4 && set.isColorable()) {
                        color2 = Integer.parseInt(data[3]);
                        FiguredataPaletteColor color = palette.getColor(color2);
                        if (color == null || (color.isClub() && !isHC)) {
                            color2 = palette.getFirstNonHCColor().getId();
                        }
                    }

                    dataParts.add(settype.getType());
                    dataParts.add("" + setId);

                    if (color1 > -1) {
                        dataParts.add("" + color1);
                    }

                    if (color2 > -1) {
                        dataParts.add("" + color2);
                    }

                    lookParts.add(String.join("-", dataParts));
                }
            } catch (Exception e) {
                //habbo.alert(e.getMessage());
                log.error("Error in clothing validation", e);
            }
        });

        return String.join(".", lookParts);
    }

}
