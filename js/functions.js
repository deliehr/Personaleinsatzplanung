var Planing = {
    EARLY_SHIFT: "EARLY_SHIFT",
    LATE_SHIFT: "LATE_SHIFT",
    FREE: "FREE",
    VACATION: "VACATION",
    EARLY_SHIFT_WEEK: "EARLY_SHIFT_WEEK",
    LATE_SHIFT_WEEK: "LATE_SHIFT_WEEK",
    MANUAL_HOURS: "MANUAL_HOURS"
};

function shiftButton(dvs, option, option_value, day_index) {
    var postCall = $.post("ajax/add_manual_constraint.jsp", { dvs: dvs, option: option, option_value: option_value, day_index: day_index})
        .done(function() {
            location.reload();
        })
        .fail(function(data) {
            alert("Fehler: " + data);
        });
}

function addHoverFunctions() {
    $(".td_shift_early, .td_shift_late, .td_shift_no_shift, .td_shift_early_r, .td_shift_late_r, .td_shift_no_shift_r").click(function(e) {
        $("#hover_menu").remove();

        // generate menu
        var divHoverMenu = document.createElement("div");
        divHoverMenu.setAttribute("id", "hover_menu");
        divHoverMenu.setAttribute("class", "hover_menu");
        divHoverMenu.style.top = e.pageY + "px";
        divHoverMenu.style.left = e.pageX + "px";

        // close button
        var closeButton = document.createElement("button");
        closeButton.appendChild(document.createTextNode("X"));
        closeButton.onclick = function() {
            document.body.removeChild(divHoverMenu);
        };

        // read hidden elements
        var firstName = $(this).attr("data-firstName"), lastName = $(this).attr("data-lastName"), dayIndex = $(this).attr("data-dayIndex"), dayName = "", dvs = $(this).attr("data-dvs"), option="", optionValue="0.0";

        // dayname
        switch(dayIndex) {
            default:
            case 0: dayName = "Montag"; break;
            case 1: dayName = "Dienstag"; break;
            case 2: dayName = "Mittwoch"; break;
            case 3: dayName = "Donnerstag"; break;
            case 4: dayName = "Freitag"; break;
            case 5: dayName = "Samstag"; break;
            case 6: dayName = "Sonntag"; break;
        }

        // display dayname and shift name
        divHoverMenu.appendChild(document.createTextNode(dayName + ": " + firstName + " " + lastName));
        divHoverMenu.appendChild(closeButton);
        divHoverMenu.appendChild(document.createElement("hr"));

        // define shift buttons
        var buttons = ["Frühschicht", Planing.EARLY_SHIFT, "Spätschicht", Planing.LATE_SHIFT, "Frei", Planing.FREE, "Urlaub", Planing.VACATION, "Woche Frühschicht", Planing.EARLY_SHIFT_WEEK, "Woche Spätschicht", Planing.LATE_SHIFT_WEEK];

        for(var i=0;i < buttons.length;i+=2) {
            if(i == 8 || i == 12) {
                var hr = document.createElement("hr");
                divHoverMenu.appendChild(hr);
            }

            var newButton = document.createElement("button");
            newButton.appendChild(document.createTextNode(buttons[i]));
            newButton.setAttribute("data-dvs", dvs);
            newButton.setAttribute("data-option", buttons[i+1]);
            newButton.setAttribute("data-optionValue", optionValue);
            newButton.setAttribute("data-dayIndex", dayIndex);
            newButton.onclick = function() {
                shiftButton($(this).attr("data-dvs"), $(this).attr("data-option"), $(this).attr("data-optionValue"), $(this).attr("data-dayIndex"));
            };
            divHoverMenu.appendChild(newButton);
        }

        // manual hour count
        divHoverMenu.appendChild(document.createElement("hr"));
        divHoverMenu.appendChild(document.createTextNode("Stunden:"));

        var inputHours = document.createElement("input");
        inputHours.name = "inputHours";
        inputHours.className = "inputHours";
        divHoverMenu.appendChild(inputHours);

        var buttonHours = document.createElement("button");
        buttonHours.appendChild(document.createTextNode("Übernehmen"));
        buttonHours.setAttribute("data-dvs", dvs);
        buttonHours.setAttribute("data-option", Planing.MANUAL_HOURS);
        buttonHours.setAttribute("data-dayIndex", dayIndex);
        buttonHours.onclick = function() {
            shiftButton($(this).attr("data-dvs"), $(this).attr("data-option"), $("input[name='inputHours']").val(), $(this).attr("data-dayIndex"));
        };
        divHoverMenu.appendChild(buttonHours);

        // manual config
        divHoverMenu.appendChild(document.createElement("hr"));
        var divManualButtons = document.createElement("div");

        var request = $.ajax({
            method: "POST",
            url: "ajax/read_manual_constraints_for_menu.jsp",
            data: { dvs: dvs, dayIndex: dayIndex },
            cache: false,
            dataType: "text",
            div: divManualButtons,
            tdDiv: this
        });

        request.done(function(data) {
            //alert(data.trim());
            data = data.trim();
            var json = $.parseJSON(data);
            var div = this.div;
            var tdDiv = this.tdDiv;

            $.each(json, function(index, restrictions) {
                $.each(restrictions, function(index, restriction) {
                    $.each(restriction, function(index, obj) {
                        var newDiv = document.createElement("div");
                        newDiv.className = "manual_constraint";

                        var newButton = document.createElement("button");
                        newButton.appendChild(document.createTextNode("X"));
                        newButton.setAttribute("data-id", obj.id);
                        newButton.onclick = function() {
                            // remove manual constraint
                            var request = $.ajax({
                                method: "POST",
                                url: "ajax/remove_manual_constraint.jsp",
                                data: { id: this.getAttribute("data-id") },
                                cache: false,
                                dataType: "text"
                            });

                            request.done(function(data) {
                                data = data.trim();

                                if(data == "1") {
                                    // remove div
                                    newDiv.remove();

                                    location.reload();
                                }
                            });
                        };

                        var label = "";
                        switch(obj.option) {
                            default:
                            case Planing.EARLY_SHIFT: { label = "Frühschicht"; break; }
                            case Planing.LATE_SHIFT: { label = "Spätschicht"; break; }
                            case Planing.EARLY_SHIFT_WEEK: { label = "Woche Frühschicht"; break; }
                            case Planing.LATE_SHIFT_WEEK: { label = "Woche Spätschicht"; break; }
                            case Planing.VACATION: { label = "Urlaub"; break; }
                            case Planing.FREE: { label = "Frei"; break; }
                            case Planing.MANUAL_HOURS: { label = "Manuelle Stunden"; break; }
                        }

                        newDiv.appendChild(document.createTextNode(label));
                        newDiv.appendChild(newButton);
                        div.appendChild(newDiv);
                    });
                });
            });
        });
        divHoverMenu.appendChild(divManualButtons);

        document.body.appendChild(divHoverMenu);
    })
}

function addConstraintFunctions() {
    $(".manual_constraint button").click(function() {
        var id = -1;
        id = $(this).attr("data-id");

        var postCall = $.post("ajax/remove_manual_constraint.jsp", { id: id})
            .done(function() {
                location.reload();
            })
            .fail(function(data) {
                alert("Fehler: " + data);
            });
    });
}