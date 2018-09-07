

  
(function () {
  
  // any link that is not part of the current domain is modified
  var links = document.links;
  for (var i = 0; i < links.length; i++) {
    if (links[i].hostname != window.location.hostname && links[i].target == "") {
      links[i].target = '_blank'
    }
  }
  
  if (page_info.title == "Events") {
    window.addEventListener("load", function() {
      var past_events_list = document.getElementById("past_events")
      var upcoming_events_list = document.getElementById("upcoming_events")
      
      var now = new Date(Date.now())
      
      var events = past_events_list.children
      
      var upcoming_events = []
      
      for (j = 0; j < events.length; j++) {
        var date = new Date(events[j].getAttribute("data-date"))
        
        if (date > now) {
          upcoming_events.push(events[j])
        } else {
          break
        }
      }
      
      for (j = 0; j < upcoming_events.length; j++) {
        past_events_list.removeChild(upcoming_events[j])
        upcoming_events_list.appendChild(upcoming_events[j])
      }
    })
  }

})()

$(document).ready(function () {})

$(document).ready(function () {
  onloadFunctions.forEach(function(f) { f() })
})

